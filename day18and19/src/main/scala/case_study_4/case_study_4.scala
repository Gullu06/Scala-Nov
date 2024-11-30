package case_study_4

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object case_study_4 {
  def main(args: Array[String]): Unit = {
    // Initialize SparkSession
    val spark = SparkSession.builder()
      .appName("GCS Parquet Processor")
      .master("local[*]")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "src/main/scala/spark-gcs-key.json")
      .getOrCreate()

    // GCS paths
    val inputPath = "gs://priyanshi-bucket/input/"
    val outputPath = "gs://priyanshi-bucket/output/processed_data.parquet"

    // Read the Parquet file from GCS
    val inputData = spark.read
      .parquet(inputPath)

    println("Input Data Schema:")
    inputData.printSchema()

    // Process the data: Filter rows where the status column equals "completed"
    val filteredData = inputData.filter(col("order_status") === "completed")

    println("Filtered Data Schema:")
    filteredData.printSchema()

    // Write the processed data back to GCS in Parquet format
    filteredData.write
      .mode("overwrite") // Overwrite the output if it already exists
      .parquet(outputPath)

    println(s"Filtered data successfully written to $outputPath")

    // Stop the SparkSession
    spark.stop()
  }
}

