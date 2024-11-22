import org.apache.spark.{SparkConf, SparkContext}

object Question6 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("charFrequency").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val rdd1 = sc.parallelize(List((1, "Ajay"), (2, "Bob"), (3, "Cat")))
    val rdd2 = sc.parallelize(List((1, 35), (2, 32), (3, 38)))

    // Join on `id`
    val joinedRDD = rdd1.join(rdd2)
    val result = joinedRDD.map { case (id, (name, age)) => (id, name, age) }
    result.collect().foreach(println)
    sc.stop()
  }
}
