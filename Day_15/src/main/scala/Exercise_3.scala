import org.apache.spark.{SparkConf, SparkContext}

object Exercise_3 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("WordCount").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Create an RDD of 1 million lines of text (e.g., repetitive "lorem ipsum" text)
    val loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
    val lines = sc.parallelize(Seq.fill(1000000)(loremIpsum))

    val wordCounts = lines
      .flatMap(line => line.split(" "))  // Split each line into words
      .map(word => (word, 1))            // Map each word to (word, 1)
      .reduceByKey(_ + _)                // Reduce by key (sum the counts)

    // Trigger the computation and collect the results (this is a blocking operation)
    val result = wordCounts.collect()

    // Optionally, you can save the result to a file instead of collecting
    // wordCounts.saveAsTextFile("output/wordcounts")


    scala.io.StdIn.readLine()
    sc.stop()
  }
}
