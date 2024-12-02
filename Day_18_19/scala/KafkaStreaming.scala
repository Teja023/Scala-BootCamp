import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.streaming.Trigger


object KafkaStreaming {
  def main(args: Array[String]): Unit = {
    // Initialize SparkSession
    val spark = SparkSession.builder()
      .appName("kafka streaming")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "false")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/tejaparvathala/spark/local_spark/key.json")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val kafkaBrokers = "localhost:9092"
    val kafkaTopic = "transaction"

    val schema = new StructType()
      .add("transactionId", StringType, true)
      .add("userId", StringType, true)
      .add("amount", DoubleType, true)

    val rawStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBrokers)
      .option("subscribe", kafkaTopic)
      .load()

    val transactionsStream = rawStream
      .selectExpr("CAST(value AS STRING) as json_data", "timestamp as kafka_timestamp")
      .select(from_json(col("json_data"), schema).as("data"), col("kafka_timestamp"))
      .withColumn("event_timestamp", col("kafka_timestamp").cast(TimestampType))
      .select("data.transactionId", "data.userId", "data.amount", "event_timestamp")

    // Debugging: Print incoming data to the console
    transactionsStream.writeStream
      .outputMode("append")
      .format("console")
      .start()

    // Apply windowing with a 10-second window
    val windowedStream = transactionsStream
      .withWatermark("event_timestamp", "10 seconds") // 10-second watermark
      .groupBy(window(col("event_timestamp"), "10 seconds").as("window")) // 10-second window
      .agg(sum("amount").as("total_amount"))

    // Output windowed aggregation results to the console
    windowedStream.writeStream
      .outputMode("update") // Use "update" mode for incremental aggregations
      .format("console")
      .trigger(Trigger.ProcessingTime("10 seconds")) // Process batches every 10 seconds
      .start()
      .awaitTermination()

  }
}
