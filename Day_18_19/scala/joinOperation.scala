import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions.broadcast


object joinOperation {
  def main(args: Array[String]): Unit = {
    // Initialize SparkSession with GCS configurations
    val spark = SparkSession.builder()
      .appName("Dataset Join using broadcasting")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/tejaparvathala/spark/local_spark/key.json")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Load the small dataset (User Details)
    val userDetails = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("/Users/tejaparvathala/spark/broadcasting/src/main/dataset/user_details.csv") // Replace with actual path

    // Load the large dataset (Transaction Logs)
    val transactionLogs = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("/Users/tejaparvathala/spark/broadcasting/src/main/dataset/transaction_details.csv") // Replace with actual path

    // Record start time
    val startTime = System.nanoTime()

    // Perform the join using broadcasting
    val joinedDF = transactionLogs
      .join(broadcast(userDetails), Seq("user_id"), "inner")

    // Show the result (for demonstration purposes)
    joinedDF.show()

    // Record end time
    val endTime = System.nanoTime()

    // Calculate time taken
    val timeTaken = (endTime - startTime) / 1e9 // Convert nanoseconds to seconds

    println(s"Time taken for the join operation: $timeTaken seconds")

    // Write CSV data to GCS
    val outputPath = "gs://movielens_dataset/joined_csv_parquet/"
//    joinedDF.write
//      .option("header", "true")
//      .mode("overwrite")
//      .csv(outputPath)

    joinedDF.write
      .mode("overwrite")  // You can also use "append" or "ignore" as needed
      .parquet(outputPath)

    println(s"CSV successfully written to $outputPath")

    spark.stop()
  }
}