import org.apache.spark.{SparkConf, SparkContext}

object Question7 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("union").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val rdd1 = sc.parallelize(List(1, 2, 3, 4))
    val rdd2 = sc.parallelize(List(3, 4, 5, 6))

    // Union and remove duplicates
    val unionRDD = rdd1.union(rdd2).distinct()
    unionRDD.collect().foreach(println)
    sc.stop()
  }
}
