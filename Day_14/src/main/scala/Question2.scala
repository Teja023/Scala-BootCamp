import org.apache.spark.{SparkConf, SparkContext}

object Question2 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("CartesianProduct").setMaster("local[1]")
    val sc = new SparkContext(conf)
    val rdd1 = sc.parallelize(List(2, 3, 4))
    val rdd2 = sc.parallelize(List(2, 3, 4))
    // Cartesian product
    val cartesianProduct = rdd1.cartesian(rdd2)
    cartesianProduct.collect().foreach(println)
    sc.stop()
  }
}
