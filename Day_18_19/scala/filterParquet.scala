import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions.broadcast


object filterParquet {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Dataset filter on parquet")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/tejaparvathala/spark/local_spark/key.json")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Path to the Parquet file in GCS
    val inputPath = "gs://movielens_dataset/joined_csv_parquet/"

    // Read the Parquet file from GCS
    val df = spark.read.parquet(inputPath)

    // Apply a filter on the DataFrame (example: filter by 'status' column)
    val filteredDF = df.filter("status = 'Success'")

    // Show the filtered DataFrame
    filteredDF.show()

    val outputPath = "gs://movielens_dataset/filtered_parquet/"

    filteredDF.write
      .mode("overwrite")  // You can also use "append" or "ignore" as needed
      .parquet(outputPath)

    spark.stop()
  }
}