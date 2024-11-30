package case_study_4

import org.apache.spark.sql.SparkSession

object inputParquetData {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Generate Parquet in GCS with Updated Schema")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "src/main/scala/spark-gcs-key.json")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Updated sample data for Parquet file
    val orderData = Seq(
      (101, "Alice", "delivered", 150.25),
      (102, "Bob", "pending", 200.0),
      (103, "Charlie", "shipped", 99.99),
      (104, "David", "completed", 0.0),
      (105, "Eva", "delivered", 300.5),
      (106, "Frank", "completed", 250.75),
      (107, "Grace", "pending", 100.25),
      (108, "Hank", "delivered", 450.0),
      (109, "Ivy", "completed", 0.0),
      (110, "Jack", "shipped", 399.99),
      (111, "Karen", "delivered", 500.5),
      (112, "Leo", "pending", 150.0),
      (113, "Mona", "shipped", 120.75),
      (114, "Nick", "completed", 0.0),
      (115, "Oscar", "delivered", 250.0),
      (116, "Pam", "pending", 75.5),
      (117, "Quinn", "completed", 180.0),
      (118, "Rose", "delivered", 600.0),
      (119, "Sam", "pending", 300.5),
      (120, "Tina", "completed", 0.0)
    )

    // Define schema with new columns
    val ordersDf = orderData.toDF("order_id", "customer_name", "order_status", "order_amount")

    // Output GCS path
    val outputPath = "gs://priyanshi-bucket/input"

    // Write DataFrame to Parquet format in GCS
    ordersDf.write.parquet(outputPath)

    println(s"Parquet file successfully written to $outputPath")

    spark.stop()
  }
}
