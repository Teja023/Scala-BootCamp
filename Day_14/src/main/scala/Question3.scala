import org.apache.spark.{SparkConf, SparkContext}

object Question3 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("evenNumbers").setMaster("local[1]")
    val sc = new SparkContext(conf)
    val numbers = sc.parallelize(List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12))

    // Filter out even numbers
    val oddNumbers = numbers.filter(_ % 2 == 0)
    oddNumbers.collect().foreach(println)
    sc.stop()
  }
}
