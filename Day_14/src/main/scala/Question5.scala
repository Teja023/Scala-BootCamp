import org.apache.spark.{SparkConf, SparkContext}

object Question5 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("average").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val scores = sc.parallelize(List((1, 80), (2, 90), (3, 70), (4, 100), (5, 10), (6, 1)))

    // Compute total score and count
    val (totalScore, count) = scores.map(_._2).aggregate((0, 0))(
      (acc, value) => (acc._1 + value, acc._2 + 1),
      (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2)
    )

    val averageScore = totalScore.toDouble / count
    println(s"Average score: $averageScore")
    sc.stop()
  }
}
