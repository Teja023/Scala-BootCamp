import org.apache.spark.sql.{DataFrame, SparkSession, functions => F}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.storage.StorageLevel
import org.apache.spark.sql.types._
import org.apache.spark.sql.protobuf.functions.from_protobuf

object KafkaDataProcessing {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Walmart Data Processing")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    // Kafka configuration
    val path_to_dataset  = "/Users/tejaparvathala/spark/caseStudy4/src/main/Dataset"
    val kafkaBrokers = "localhost:9092"
    val kafkaTopic = "weekly_sales"
    //path to datasets
    val features_df_path = s"$path_to_dataset/features.csv"
    val stores_df_path = s"$path_to_dataset/stores.csv"
    val parquet_path = s"$path_to_dataset/historical_data"
    val store_metrics_path = s"$path_to_dataset/store_metrics"
    val department_metrics_path = s"$path_to_dataset/department_metrics"
    val weekly_trend_metrics_path = s"$path_to_dataset/weekly_trend"
    val holiday_metrics_path = s"$path_to_dataset/holiday_metrics"
    val descriptorFile = "src/main/scala/protobuf/descriptor/SalesData.desc"
    val messageType = "protobuf.salesData"

    // Historical data schema
    val salesSchema = StructType(Array(
      StructField("Store", StringType),
      StructField("Dept", StringType),
      StructField("Date", StringType),
      StructField("Weekly_Sales", DoubleType),
      StructField("IsHoliday", BooleanType)
    ))

    // Reading Kafka stream
    val rawStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBrokers)
      .option("subscribe", kafkaTopic)
      .option("startingOffsets", "earliest")
      .load()

    import spark.implicits._
    // Extract Protobuf binary data and deserialize to DataFrame
    val salesStreamDF = rawStream
      .selectExpr("CAST(value AS BINARY) as value") // Extract binary Protobuf data
      .select(from_protobuf($"value", messageType, descriptorFile).alias("salesData")) // Deserialize Protobuf
      .select("SalesData.*") // Flatten the struct for individual fields
      .select(
        $"store".alias("Store"),
        $"dept".alias("Dept"),
        $"date".alias("Date"),
        $"weekly_sales".alias("Weekly_Sales"),
        $"is_holiday".alias("IsHoliday")
      )

    // Loading static datasets
    val featuresDF = spark.read.option("header", "true").option("inferSchema", "true")
      .csv(features_df_path)

    val storesDF = spark.read.option("header", "true").option("inferSchema", "true")
      .csv(stores_df_path)

    // Cleaning static datasets
    val cleanedFeaturesDF = featuresDF
      .na.fill(0, Seq("Temperature", "Fuel_Price", "CPI", "Unemployment"))
      .na.drop(Seq("Store", "Date"))

    val cleanedStoresDF = storesDF.na.drop(Seq("Store"))

    // Streaming logic
    val query = salesStreamDF.writeStream
      .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
        if (!batchDF.isEmpty) {
          try {
            println(s"Processing batch $batchId")

            // Step 1: Append new data to historical Parquet file
            batchDF.write
              .mode("append")
              .partitionBy("Store", "Dept")
              .parquet(parquet_path)

            // Step 2: Read combined data from Parquet file
            val historicalDataDF = spark.read
              .schema(salesSchema)
              .parquet(parquet_path)
              .drop("IsHoliday")

            // Step 3: Join with static datasets
            val enrichedDataDF = historicalDataDF
              .join(cleanedFeaturesDF, Seq("Store", "Date"), "left")
              .join(broadcast(cleanedStoresDF), Seq("Store"), "left")

            // Step 4: Compute metrics

            // Store-Level Metrics
            val storeMetricsDF = enrichedDataDF
              .groupBy("Store")
              .agg(
                sum("Weekly_Sales").alias("Total_Weekly_Sales"),
                avg("Weekly_Sales").alias("Average_Weekly_Sales")
              )

            storeMetricsDF.write
              .mode("overwrite")
              .json(store_metrics_path)

            // Department-Level Metrics
            val departmentMetricsDF = enrichedDataDF
              .groupBy("Dept")
              .agg(
                sum("Weekly_Sales").alias("Total_Sales"),
                avg("Weekly_Sales").alias("Average_Weekly_Sales")
              )

            departmentMetricsDF.write
              .mode("overwrite")
              .json(department_metrics_path)

            // Weekly Sales Trends
            val weeklyTrendsDF = enrichedDataDF
              .groupBy("Dept", "Date")
              .agg(
                avg("Weekly_Sales").alias("Average_Weekly_Sales")
              )
              .orderBy("Dept", "Date")

            weeklyTrendsDF.write
              .mode("overwrite")
              .json(weekly_trend_metrics_path)

            // Holiday vs. Non-Holiday Sales
            val holidaySalesDF = enrichedDataDF
              .groupBy("Dept", "IsHoliday")
              .agg(
                sum("Weekly_Sales").alias("Total_Sales"),
                avg("Weekly_Sales").alias("Average_Weekly_Sales")
              )

            holidaySalesDF.write
              .mode("overwrite")
              .json(holiday_metrics_path)

            println(s"Batch $batchId processed successfully")
          } catch {
            case e: Exception =>
              println(s"Error processing batch $batchId: ${e.getMessage}")
          }
        } else {
          println(s"No data in batch $batchId")
        }
      }
      .outputMode("append")
      .trigger(Trigger.ProcessingTime("1 minute"))
      .start()

    query.awaitTermination()
  }
}
