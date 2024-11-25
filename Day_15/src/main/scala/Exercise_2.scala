import org.apache.spark.{SparkConf, SparkContext}

object Exercise_2 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Narrow-Wide-Transformations").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val numbers = sc.parallelize(1 to 1000, numSlices = 4)

    val mappedNumbers = numbers.map(_ * 2) // Multiply each number by 2
    val filteredNumbers = mappedNumbers.filter(_ % 3 == 0) // Keep only numbers divisible by 3

    val keyValuePairs = filteredNumbers.map(num => (num % 10, num)) // Map to key-value pairs
    val groupedNumbers = keyValuePairs.groupByKey() // Wide transformation (causes a shuffle)

    groupedNumbers.saveAsTextFile("output/grouped_numbers")

    scala.io.StdIn.readLine()
    sc.stop()
  }
}
