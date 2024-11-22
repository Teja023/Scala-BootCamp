import org.apache.spark.{SparkConf, SparkContext}

object Question10 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("charFrequency").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val kvPairs = sc.parallelize(List(("C", 12), ("B", 7), ("A", 2), ("B", 3), ("A", 4), ("B", 5), ("C", 6)))

    // Group by key and compute sum
    val groupedSum = kvPairs.groupByKey().mapValues(_.sum)
    groupedSum.collect().foreach(println)
    sc.stop()
  }
}
