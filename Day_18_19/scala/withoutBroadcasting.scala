import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions.broadcast


object withoutBroadcasting {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Dataset Join without using broadcasting")
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
    val joinedDF = transactionLogs.join(
      userDetails,
      transactionLogs("user_id") === userDetails("user_id"),
      "inner"
    )

    // Show the result (for demonstration purposes)
    joinedDF.show()

    // Record end time
    val endTime = System.nanoTime()

    // Calculate time taken
    val timeTaken = (endTime - startTime) / 1e9 // Convert nanoseconds to seconds

    println(s"Time taken for the join operation: $timeTaken seconds")

    val outputPath = "gs://movielens_dataset/user_details_csv"
    userDetails.write
      .option("header", "true")
      .mode("overwrite")
      .csv(outputPath)

    spark.stop()
  }
}