import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object caching {
  def main(args: Array[String]): Unit = {
    // Initialize SparkSession
    val spark = SparkSession.builder()
      .appName("caching")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/tejaparvathala/spark/local_spark/key.json")
      .master("local[*]")
      .getOrCreate()

    // Load a sample dataset (Sales Data)
    val salesDataPath = "/Users/tejaparvathala/spark/broadcasting/src/main/dataset/mobiles.csv"
    val salesDF = spark.read.option("header", "true").csv(salesDataPath)

    // Print schema of salesData
    salesDF.printSchema()

    // Without caching: First transformation (filtering)
    val filteredDF = salesDF.filter("price > 25000")

    // Action to trigger execution (without caching)
    val startWithoutCache = System.currentTimeMillis()
    filteredDF.show()  // First access
    filteredDF.show()  // Second access
    val endWithoutCache = System.currentTimeMillis()

    println(s"Time taken without caching: ${endWithoutCache - startWithoutCache} ms")

    // Apply caching to the filtered DataFrame
    val cachedDF = filteredDF.cache()

    // Perform the same transformations again with caching
    val startWithCache = System.currentTimeMillis()
    cachedDF.show()  // First access (uses cached data)
    cachedDF.show()  // Second access (uses cached data)
    val endWithCache = System.currentTimeMillis()

    println(s"Time taken with caching: ${endWithCache - startWithCache} ms")

    // Stop the Spark session
    spark.stop()
  }
}
