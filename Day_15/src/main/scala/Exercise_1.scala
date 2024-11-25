import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random

object Exercise_1 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("RDD-Partitioning").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val largeData = Seq.fill(1000)(Random.nextInt(1000).toString) // 10 million random numbers
    val rdd = sc.parallelize(largeData)

    println(s"Initial number of partitions: ${rdd.getNumPartitions}")

    val repartitionedRDD = rdd.repartition(4)
    println(s"Number of partitions after repartitioning: ${repartitionedRDD.getNumPartitions}")

    println("\nData distribution after repartitioning:")
    val repartitionedDistribution = repartitionedRDD.mapPartitionsWithIndex((index, iter) =>
      Iterator((index, iter.size))
    ).collect()
    repartitionedDistribution.foreach { case (partition, count) =>
      println(s"Partition $partition: $count elements")
    }

    val coalescedRDD = repartitionedRDD.coalesce(2)
    println(s"Number of partitions after coalescing: ${coalescedRDD.getNumPartitions}")

    println("\nData distribution after coalescing:")
    val coalescedDistribution = coalescedRDD.mapPartitionsWithIndex((index, iter) =>
      Iterator((index, iter.size))
    ).collect()
    coalescedDistribution.foreach { case (partition, count) =>
      println(s"Partition $partition: $count elements")
    }

    println("\nFirst 5 elements from each partition:")
    val partitionElements = coalescedRDD.mapPartitionsWithIndex((index, iter) =>
      Iterator((index, iter.take(5).toList))
    ).collect()
    partitionElements.foreach { case (partition, elements) =>
      println(s"Partition $partition: ${elements.mkString(", ")}")
    }

    scala.io.StdIn.readLine()
    sc.stop()
  }
}
