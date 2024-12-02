import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col


object enrichedData {
  def main(args: Array[String]): Unit = {
    // Initialize SparkSession
    val spark = SparkSession.builder()
      .appName("broadcast with kafka streaming")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "false")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/tejaparvathala/spark/local_spark/key.json")
      .master("local[*]")
      .getOrCreate()

    val df = spark.read.json("gs://movielens_dataset/enriched_orders/")
    df.show()

    // Get the userId absence data
    df.filter(col("name") === "UNKNOWN" && col("age") === 0 && col("email") === "NOT_AVAILABLE")
      .show()

    spark.stop()

  }
}
