import org.apache.spark.{SparkConf, SparkContext}

object Exercise_4 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Integers").setMaster("local[2]")
    val sc = new SparkContext(conf)

    val numbers = sc.parallelize(1 to 10000)

    val result = numbers
      .filter(_ % 2 == 0)                 // Keep only even numbers
      .map(_ * 10)                        // Multiply each number by 10
      .map(num => (num % 100, num))       // Generate a tuple (remainder, number)
      .reduceByKey(_ + _)                 // Group by remainder and sum values

    val finalResult = result.collect()

    // Print the result
    finalResult.foreach { case (key, value) => println(s"Remainder: $key, Sum: $value") }

    scala.io.StdIn.readLine()
    sc.stop()
  }
}