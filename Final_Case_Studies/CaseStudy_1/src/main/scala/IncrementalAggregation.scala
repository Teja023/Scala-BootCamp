package IncrementalAggregation

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.protobuf.functions._
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.functions._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object IncrementalAggregation {
  // Incremental Aggregation is performed with respect to the processingTime
  def batchwiseIncrementalAggregation(spark: SparkSession, batchDF: DataFrame, currentProcessingTime: LocalDateTime): Unit = {
    import spark.implicits._

    val hadoopConf = spark.sparkContext.hadoopConfiguration
    val fs = FileSystem.get(hadoopConf)

    val path = "/Users/tejaparvathala/spark/caseStudy1/src/main"

    val AggregationDataParentPath = s"$path/dataset/aggregated/protobuf"
    val aggregatedDataProtoDescriptorPath = s"$path/scala/protobuf/descriptors/AggregatedSensorData.desc"
    val AggregationJsonDataParentPath = s"$path/dataset/aggregated/json"

    val aggregatedMessageType: String = "protobuf.AggregatedSensorData"

    def readExistingAggregatedData(fp: String): DataFrame = {
      spark.read.parquet(fp)
        .select(from_protobuf($"value", aggregatedMessageType, aggregatedDataProtoDescriptorPath).alias("aggregatedData"))
        .select(
          "aggregatedData.sensorId",
          "aggregatedData.averageTemperature",
          "aggregatedData.averageHumidity",
          "aggregatedData.minimumTemperature",
          "aggregatedData.maximumTemperature",
          "aggregatedData.minimumHumidity",
          "aggregatedData.maximumHumidity",
          "aggregatedData.noOfRecords"
        )
    }

    try{
      // Load existing aggregated data
      val previousProcessingTime = currentProcessingTime.minusHours(1)
      val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH")
      val currentHourAggregatedFolder = s"${AggregationDataParentPath}/${currentProcessingTime.format(formatter)}"
      val previousHourAggregatedFolder = s"${AggregationDataParentPath}/${previousProcessingTime.format(formatter)}"

      val existingAggregatedData: DataFrame = {
        if (fs.exists(new Path(currentHourAggregatedFolder))) readExistingAggregatedData(currentHourAggregatedFolder)
        else if (fs.exists(new Path(previousHourAggregatedFolder))) readExistingAggregatedData(previousHourAggregatedFolder)
        else null // for the first time aggregation
      }

      if(existingAggregatedData != null) existingAggregatedData.cache()  // repeated usage

      // Perform Incremental Aggregation
      val updatedAggregatedDF = computeAggregatedData(existingAggregatedData, batchDF).cache()  // repeated usage
      // updatedAggregatedDF.show()

      // Store these metrics inside the GCP
      val aggregatedProtoFP = s"${AggregationDataParentPath}/${currentProcessingTime.format(formatter)}"
      val aggregatedJsonFP = s"${AggregationJsonDataParentPath}/${currentProcessingTime.format(formatter)}"

      // Store the JSON format
      updatedAggregatedDF.write.mode("overwrite").json(aggregatedJsonFP)

      // Store the Proto binary format
      updatedAggregatedDF
        .withColumn("value", to_protobuf(struct(updatedAggregatedDF.columns.map(col): _*), aggregatedMessageType, aggregatedDataProtoDescriptorPath))
        .select(col("value"))
        .write
        .mode("overwrite")
        .parquet(aggregatedProtoFP)
    }
    finally {
      fs.close()
    }
  }

  private def computeAggregatedData(existingAggregatedData: DataFrame, batchDF: DataFrame): DataFrame = {
    val batchDataAggregatedDF = getBatchAggregatedDF(batchDF)

    val computedAggData = existingAggregatedData match {
      case null => batchDataAggregatedDF
      case _ =>
        existingAggregatedData.join(batchDataAggregatedDF, Seq("sensorId"), "fullOuterJoin")
          .select(
            coalesce(batchDataAggregatedDF("sensorId"), existingAggregatedData("storeId")).alias("storeId"),
            (
              (
                (coalesce(batchDataAggregatedDF("averageTemperature"), lit(0.0f)) * coalesce(batchDataAggregatedDF("noOfRecords"), lit(0)))
                  +(coalesce(existingAggregatedData("averageTemperature"), lit(0.0f)) * coalesce(existingAggregatedData("noOfRecords"), lit(0)))
                )/(
                coalesce(batchDataAggregatedDF("noOfRecords"), lit(0)) + coalesce(existingAggregatedData("noOfRecords"), lit(0))
                )
              ).cast("float")
              .alias("averageTemperature"),
            (
              (
                (coalesce(batchDataAggregatedDF("averageHumidity"), lit(0.0f)) * coalesce(batchDataAggregatedDF("noOfRecords"), lit(0)))
                  +(coalesce(existingAggregatedData("averageHumidity"), lit(0.0f)) * coalesce(existingAggregatedData("noOfRecords"), lit(0)))
                )/(
                coalesce(batchDataAggregatedDF("noOfRecords"), lit(0)) + coalesce(existingAggregatedData("noOfRecords"), lit(0))
                )
              ).cast("float")
              .alias("averageHumidity"),
            least(batchDataAggregatedDF("minimumTemperature"), existingAggregatedData("minimumTemperature")).alias("minimumTemperature"),
            greatest(batchDataAggregatedDF("maximumTemperature"), existingAggregatedData("maximumTemperature")).alias("maximumTemperature"),
            least(batchDataAggregatedDF("minimumHumidity"), existingAggregatedData("minimumHumidity")).alias("minimumHumidity"),
            greatest(batchDataAggregatedDF("maximumHumidity"), existingAggregatedData("maximumHumidity")).alias("maximumHumidity"),
            (coalesce(batchDataAggregatedDF("noOfRecords"), lit(0))
              + coalesce(existingAggregatedData("noOfRecords"), lit(0))).alias("noOfRecords")
          )
    }

    computedAggData
  }

  private def getBatchAggregatedDF(batchDF: DataFrame): DataFrame = {
    batchDF
      .groupBy("sensorId")
      .agg(
        avg(col("temperature")).cast("float").alias("averageTemperature"),
        avg(col("humidity")).cast("float").alias("averageHumidity"),
        min(col("temperature")).alias("minimumTemperature"),
        max(col("temperature")).alias("maximumTemperature"),
        min(col("humidity")).alias("minimumHumidity"),
        max(col("humidity")).alias("maximumHumidity"),
        count(lit(1)).alias("noOfRecords")
      )
  }
}