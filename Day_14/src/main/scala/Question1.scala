import org.apache.spark.{SparkConf, SparkContext}

object Question1 {
  def main(args: Array[String]): Unit = {
      val conf = new SparkConf().setAppName("WordCount").setMaster("local[1]")
      val sc = new SparkContext(conf)

      val strings = List("Hello world is the first program i run", "Apache Spark is said to be fast", "Scala uses it.")
      val rdd = sc.parallelize(strings)

      val words = rdd.flatMap(_.split(" "))
      val wordCount = words.count()
      println(s"Total number of words: $wordCount")
      sc.stop()
  }
}
