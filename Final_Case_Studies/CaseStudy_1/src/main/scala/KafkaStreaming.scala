import IncrementalAggregation.IncrementalAggregation
import org.apache.spark.sql.{DataFrame, Dataset, Row, SaveMode, SparkSession}
import org.apache.spark.sql.protobuf.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.functions._
import java.time.LocalDateTime
import org.apache.spark.sql.protobuf.functions.to_protobuf
import java.time.format.DateTimeFormatter


object KafkaStreaming {
  def main(ar: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("SensorDataStreamReader")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Kafka configuration
    val bootstrapServers = "localhost:9092"
    val topic = "weekly_sales"
    val rawDataProtoDescriptorPath = "src/main/scala/protobuf/descriptors/SensorData.desc"

    // Read messages from kafka
    val kafkaDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", bootstrapServers)
      .option("subscribe", topic)
      .option("startingOffsets", "earliest")
      .load()

    // Extract the protobuf binary data from kafka and deserialize it to df and validate the df
    val sensorDataValidatedDF = kafkaDF
      .selectExpr("CAST(value AS BINARY) as value")
      .select(from_protobuf($"value", "protobuf.SensorData", rawDataProtoDescriptorPath).alias("sensorDataRecord"))
      .select("sensorDataRecord.sensorId", "sensorDataRecord.timestamp", "sensorDataRecord.temperature", "sensorDataRecord.humidity")
      .filter($"temperature".between(-40, 140) && $"humidity".between(0, 80) && $"sensorId".isNotNull) // Filter for these values

    // Process this dataframe
    val query = sensorDataValidatedDF.writeStream
      .trigger(Trigger.ProcessingTime("20 seconds"))
      .foreachBatch { (batchDF: Dataset[Row], _: Long) => processBatch(spark, batchDF) }
      .start()

    query.awaitTermination()
    spark.stop()
  }

  private def processBatch(spark: SparkSession, batchDF: DataFrame): Unit = {
    val broadcastBatchDF = broadcast(batchDF)
    val currentProcessingTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH")
    val sensorDataMessageType: String = "protobuf.SensorData"

    val path = "/Users/tejaparvathala/spark/caseStudy1/src/main"

    val EventTimeBasedRawDataPath = s"$path/dataset/event_time/raw/sensor-data"
    val ProcessingTimeBasedRawDataParentPath = s"$path/dataset/raw/sensor-data"
    val rawDataProtoDescriptorPath = s"$path/scala/protobuf/descriptors/SensorData.desc"

    val partitionedDF = broadcastBatchDF
      .withColumn("value", to_protobuf(struct(broadcastBatchDF.columns.map(col): _*), sensorDataMessageType, rawDataProtoDescriptorPath))
      .withColumn("year", from_unixtime(col("timestamp") / 1000, "yyyy")) // Convert milliseconds to seconds
      .withColumn("month", from_unixtime(col("timestamp") / 1000, "MM"))
      .withColumn("day", from_unixtime(col("timestamp") / 1000, "dd"))
      .withColumn("hour", from_unixtime(col("timestamp") / 1000, "HH"))

    // partitionedDF
    partitionedDF
      .select(col("value"), col("year"), col("month"), col("day"), col("hour"))
      .write
      .mode(SaveMode.Append)
      .partitionBy("year", "month", "day", "hour")
      .format("parquet")
      .save(EventTimeBasedRawDataPath)

    val fp = s"${ProcessingTimeBasedRawDataParentPath}/${currentProcessingTime.format(formatter)}"

    broadcastBatchDF
      .withColumn("value", to_protobuf(struct(broadcastBatchDF.columns.map(col): _*), sensorDataMessageType, rawDataProtoDescriptorPath))
      .select(col("value"))
      .write
      .mode(SaveMode.Append)
      .format("parquet")
      .save(fp)

    IncrementalAggregation.batchwiseIncrementalAggregation(spark, broadcastBatchDF, currentProcessingTime)
  }
}