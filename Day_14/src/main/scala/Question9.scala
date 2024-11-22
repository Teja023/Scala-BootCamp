import org.apache.spark.{SparkConf, SparkContext}

object Question9 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("reduce").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val rdd = sc.parallelize(1 to 100)

    // Compute the sum
    val sum = rdd.reduce(_ + _)
    println(s"Sum of integers from 1 to 100: $sum")
    sc.stop()
  }
}
