import org.apache.spark.{SparkConf, SparkContext}

object Question8 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("filter").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val csvData = List("1,Ajay,25", "2,Bob,17", "3,charan,16", "4,danny,18")
    val rdd = sc.parallelize(csvData)

    // Parse rows and filter by age
    val filtered = rdd.map(_.split(",")).filter(row => row(2).toInt >= 18)
    filtered.collect().foreach(row => println(row.mkString(",")))
    sc.stop()
  }
}
