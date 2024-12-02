import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.streaming.Trigger


object broadcastWithKafka {
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

    spark.sparkContext.setLogLevel("WARN")

    // Kafka Source Configuration
    val kafkaBrokers = "localhost:9092" // Replace with your Kafka broker(s)
    val kafkaTopic = "orders"

    // Read user details (broadcast dataset) from GCS
    val userDetailsPath = "gs://movielens_dataset/user_details_csv"
    val userDetailsDF = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(userDetailsPath)

    // Broadcast the user details DataFrame
    val broadcastUserDetails = spark.sparkContext.broadcast(userDetailsDF.collect())

    // Read data from Kafka
    val rawStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBrokers)
      .option("subscribe", kafkaTopic)
      .load()

    // Define schema for the Kafka message
    val ordersSchema = new StructType()
      .add("order_id", StringType, true)
      .add("user_id", IntegerType, true)
      .add("order_amount", DoubleType, true)

    // Parse the Kafka messages
    val ordersStream = rawStream.selectExpr("CAST(value AS STRING) as json_data")
      .select(from_json(col("json_data"), ordersSchema).as("data"))
      .select("data.*")

    // Perform the broadcast join
    val userDetailsRDD = spark.sparkContext.parallelize(broadcastUserDetails.value)
    val userDetailsBroadcastDF = spark.createDataFrame(userDetailsRDD, userDetailsDF.schema)

    val enrichedOrdersStream = ordersStream
      .join(broadcast(userDetailsBroadcastDF), Seq("user_id"), "left")

    // Write enriched data back to GCS in JSON format
    val outputPath = "gs://movielens_dataset/enriched_orders/"

    enrichedOrdersStream.writeStream
      .outputMode("append") // Append mode to continuously write data
      .format("json")
      .option("path", outputPath)
      .option("checkpointLocation", "gs://movielens_dataset/enriched_orders/")
      .start()
      .awaitTermination()
  }
}
