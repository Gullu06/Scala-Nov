package retention

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, from_unixtime, lit}

import java.time.Instant
import java.time.temporal.ChronoUnit

object DataRetentionPolicy {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Data Retention Policy")
      .master("local[*]")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    // Paths for enriched data and metrics
    val enrichedDataPath = "gs://movie_bucket_pc/enriched_ratings/"
    val parquetDataPath = s"$enrichedDataPath"
    val retentionDays = 14

    // Retain enriched raw data for the last 14 days
    cleanupOldFiles(parquetDataPath, retentionDays)

    println("Data retention policy applied successfully.")
  }

  /**
   * Cleans up files older than the specified number of days from the given path.
   *
   * @param path The GCS path to clean up.
   * @param retentionDays Number of days to retain the files.
   */
  def cleanupOldFiles(path: String, retentionDays: Int): Unit = {
    val now = Instant.now()
    val cutoffDate = now.minus(retentionDays, ChronoUnit.DAYS)
    val cutoffTimestamp = cutoffDate.toEpochMilli / 1000 // Convert to epoch seconds

    println(s"Cleaning up files older than $retentionDays days in $path.")

    try {
      // Use Spark to list files
      val files = SparkSession.active.read.format("binaryFile").load(path)

      // Convert BIGINT to TIMESTAMP and filter files
      val oldFiles = files
        .select("path", "modificationTime")
        .filter(col("modificationTime") < from_unixtime(lit(cutoffTimestamp))) // Convert BIGINT to TIMESTAMP

      // Collect the paths of old files and delete them
      oldFiles.select("path").collect().foreach { row =>
        val filePath = row.getString(0)
        println(s"Deleting file: $filePath")
        val fs = org.apache.hadoop.fs.FileSystem.get(SparkSession.active.sparkContext.hadoopConfiguration)
        val hadoopPath = new org.apache.hadoop.fs.Path(filePath)
        fs.delete(hadoopPath, false)
      }

      println("File cleanup completed.")
    } catch {
      case e: Exception =>
        println(s"Error during cleanup: ${e.getMessage}")
    }
  }
}
