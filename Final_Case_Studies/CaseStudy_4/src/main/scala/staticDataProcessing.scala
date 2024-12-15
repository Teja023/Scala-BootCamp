import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.storage.StorageLevel

object staticDataProcessing {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Walmart Data Processing")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val path_to_dataset  = "/Users/tejaparvathala/spark/caseStudy4/src/main/Dataset"
    val train_df_path = s"$path_to_dataset/train.csv"
    val features_df_path = s"$path_to_dataset/features.csv"
    val stores_df_path = s"$path_to_dataset/stores.csv"
    val parquet_path = s"$path_to_dataset/historical_data"
    val store_metrics_path = s"$path_to_dataset/store_metrics"
    val department_metrics_path = s"$path_to_dataset/department_metrics"
    val weekly_trend_metrics_path = s"$path_to_dataset/weekly_trend"
    val holiday_metrics_path = s"$path_to_dataset/holiday_metrics"


    // Load Datasets
    val trainDF = spark.read.option("header", "true").option("inferSchema", "true")
      .csv(train_df_path)

    val featuresDF = spark.read.option("header", "true").option("inferSchema", "true")
      .csv(features_df_path)

    val storesDF = spark.read.option("header", "true").option("inferSchema", "true")
      .csv(stores_df_path)

    // Validate Train Data
    val cleanedTrainDF = trainDF
      .filter(col("Weekly_Sales") >= 0) // Remove negative Weekly_Sales
      .na.drop(Seq("Store", "Dept", "Date", "Weekly_Sales"))
      .drop("IsHoliday")

    // Validate and Clean Features Data
    val cleanedFeaturesDF = featuresDF
      .na.fill(0, Seq("Temperature", "Fuel_Price", "CPI", "Unemployment")) // Fill missing numerical values
      .na.drop(Seq("Store", "Date")) // Drop rows with nulls in critical columns

    // Validate and Clean Stores Data
    val cleanedStoresDF = storesDF.na.drop(Seq("Store"))

    // Cache cleaned features and stores data
    cleanedFeaturesDF.cache()
    cleanedStoresDF.cache()

    println("Cleaned Train Data")
    cleanedTrainDF.show(10)

    // Perform transformations and join with features and stores data
    val enrichedDFWithFeatures = cleanedTrainDF
      .join(cleanedFeaturesDF, Seq("Store", "Date"), "left")  // Join with features
      .join(broadcast(cleanedStoresDF), Seq("Store"), "left")  // Join with stores

    // Perform any other transformations or actions you need
    println("joined data")
    enrichedDFWithFeatures.show(10)

    val enrichedDFPartitioned = enrichedDFWithFeatures
      .repartition(col("Store"), col("Dept")) // Partition data in memory
      .cache() // Cache for reuse in downstream tasks

    // Write partitioned data to Parquet format
    cleanedTrainDF.limit(1000).write
      .mode("Overwrite")
      .partitionBy("Store", "Dept") // Physically partition the data
      .parquet(parquet_path)

    // Store-Level Aggregations
    val storeMetricsDF = enrichedDFPartitioned
      .groupBy("Store")
      .agg(
        sum("Weekly_Sales").alias("Total_Weekly_Sales"),
        avg("Weekly_Sales").alias("Average_Weekly_Sales")
      )
      .persist(StorageLevel.MEMORY_AND_DISK_SER) // Cache intermediate results for reuse

    storeMetricsDF.write
      .mode("overwrite")
      .json(store_metrics_path)

    // Top Stores by Total Sales
    val topStoresDF = storeMetricsDF
      .orderBy(desc("Total_Weekly_Sales"))
      .limit(10) // Adjust limit as needed for top stores

    // Department-Level Aggregations
    val departmentMetricsDF = enrichedDFPartitioned
      .groupBy("Dept")
      .agg(
        sum("Weekly_Sales").alias("Total_Sales"),
        avg("Weekly_Sales").alias("Average_Weekly_Sales")
      )
      .persist(StorageLevel.MEMORY_AND_DISK_SER)

    departmentMetricsDF.write
      .mode("overwrite")
      .json(department_metrics_path)

    // Weekly Sales Trends
    val weeklyTrendsDF = enrichedDFPartitioned
      .groupBy("Dept", "Date")
      .agg(
        avg("Weekly_Sales").alias("Average_Weekly_Sales")
      )
      .orderBy("Dept", "Date")

    weeklyTrendsDF.write
      .mode("overwrite")
      .json(weekly_trend_metrics_path)

    // Holiday vs. Non-Holiday Sales
    val holidaySalesDF = enrichedDFPartitioned
      .groupBy("Dept", "IsHoliday")
      .agg(
        sum("Weekly_Sales").alias("Total_Sales"),
        avg("Weekly_Sales").alias("Average_Weekly_Sales")
      )
      .orderBy(col("Dept"), desc("Total_Sales"))


    holidaySalesDF.write
      .mode("overwrite")
      .json(holiday_metrics_path)

    println("Stores weekly Data")
    storeMetricsDF.show(10)
    println("Top Stores weekly Data")
    topStoresDF.show()
    println("department wise weekly Data")
    departmentMetricsDF.show(10)
    println("weekly Trends Data")
    weeklyTrendsDF.show(10)
    println("Holiday wise weekly Trends Data")
    holidaySalesDF.show(10)

    spark.stop()

  }
}
