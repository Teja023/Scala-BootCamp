import org.apache.spark.{SparkConf, SparkContext}

object Exercise_5 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("impact_of_partition").setMaster("local[2]")
    val sc = new SparkContext(conf)

    // Load the dataset (replace "path_to_file.csv" with your actual dataset path)
    val largeDataset = sc.textFile("src/main/scala/sample.csv")

    // Count the rows in the dataset
    val rowCount = largeDataset.count()
    println(s"Total number of rows: $rowCount")

    // Partition into 2 partitions, sort, and write to disk
    val partitionedRDD2 = largeDataset.repartition(2)
    val sortedRDD2 = partitionedRDD2.sortBy(row => row)
    sortedRDD2.saveAsTextFile("output_2_partitions")

    // Partition into 4 partitions, sort, and write to disk
    val partitionedRDD4 = largeDataset.repartition(4)
    val sortedRDD4 = partitionedRDD4.sortBy(row => row)
    sortedRDD4.saveAsTextFile("output_4_partitions")

    // Partition into 8 partitions, sort, and write to disk
    val partitionedRDD8 = largeDataset.repartition(8)
    val sortedRDD8 = partitionedRDD8.sortBy(row => row)
    sortedRDD8.saveAsTextFile("output_8_partitions")


    scala.io.StdIn.readLine()
    sc.stop()
  }
}