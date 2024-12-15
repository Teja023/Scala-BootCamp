package Retention

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.SparkSession
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object Retention {
  def main(ar: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Retention of Data")
      .master("local[*]")
      .getOrCreate()

    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH")
    val cutOffTime = LocalDateTime.now().minus(1, ChronoUnit.DAYS)
    val ProcessingTimeBasedRawDataParentPath = "/Users/tejaparvathala/spark/caseStudy1/src/main/dataset/raw/sensor-data"

    val fs = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    val yearPaths = fs.listStatus(new Path(ProcessingTimeBasedRawDataParentPath)).map(_.getPath)
    deleteOldFolders(yearPaths, fs, cutOffTime, formatter)

    spark.stop()
  }

  def deleteOldFolders(yearPaths: Seq[Path], fs: FileSystem, cutOffTime: LocalDateTime, formatter: DateTimeFormatter) = {
    val paths = yearPaths
      .iterator
      .flatMap(yearPath =>
        fs.listStatus(yearPath)
          .iterator
          .flatMap(monthPath =>
            fs.listStatus(monthPath.getPath)
              .iterator
              .flatMap(dayPath =>
                fs.listStatus(dayPath.getPath)
                  .iterator
                  .map(hourPath => (yearPath, monthPath, dayPath, hourPath))
              )
          )
      )

    //      paths.map { case (yearPath, monthPath, dayPath, hourPath) => s"${yearPath.getName}/${monthPath.getPath.getName}/${dayPath.getPath.getName}/${hourPath.getPath.getName}" }
    //      .foreach(println)

    paths.filter { case (yearPath, monthPath, dayPath, hourPath) =>
        val fullPath = s"${yearPath.getName}/${monthPath.getPath.getName}/${dayPath.getPath.getName}/${hourPath.getPath.getName}"
        val folderTimestamp = LocalDateTime.parse(fullPath, formatter)
        folderTimestamp.isBefore(cutOffTime)
        //        folderTimestamp.isAfter(cutOffTime)
      }
      .foreach { case (_, _, _, hourPath) =>
        val path = hourPath.getPath
        //        println(path)
        fs.delete(path, true)
      }
  }

}