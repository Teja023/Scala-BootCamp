import org.apache.spark.{SparkConf, SparkContext}

object Question4 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("charFrequency").setMaster("local[*]")
    val sc = new SparkContext(conf)
    // Input collection of strings
    val strings = List("hello", "world", "apache", "spark", "scala", "akka", "play")

    // Create an RDD
    val rdd = sc.parallelize(strings)

    // Split each string into characters and flatten the result
    val chars = rdd.flatMap(_.toCharArray)

    // Map each character to a count of 1
    val charCounts = chars.map(char => (char, 1))

    // Aggregate character counts without `reduceByKey`
    val frequency = charCounts
      .groupByKey()
      .map { case (char, counts) => (char, counts.sum) }

    // Collect and print the results
    frequency.collect().foreach { case (char, count) =>
      println(s"Character '$char' appears $count times.")
    }

    // Stop the Spark context
    sc.stop()
  }
}
