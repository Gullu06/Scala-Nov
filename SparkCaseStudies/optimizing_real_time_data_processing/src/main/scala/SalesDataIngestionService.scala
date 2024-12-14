import org.apache.spark.sql.{Dataset, Row, SaveMode, SparkSession}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._
import org.apache.spark.storage.StorageLevel
import scala.concurrent.duration._

object SalesDataIngestionService {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Real-Time Sales Consumer")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    // Kafka Configuration
    val kafkaStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "weekly_sales")
      .option("startingOffsets", "latest")
      .load()

    // Schema Definitions
    val salesSchema = StructType(Seq(
      StructField("Store", StringType),
      StructField("Dept", StringType),
      StructField("Weekly_Sales", DoubleType),
      StructField("Date", StringType)
    ))



    val storesSchema = StructType(Seq(
      StructField("Store", StringType),
      StructField("Type", StringType),
      StructField("Size", IntegerType)
    ))

    val featuresSchema = StructType(Seq(
      StructField("Store", StringType),
      StructField("Date", StringType),
      StructField("Temperature", DoubleType),
      StructField("Fuel_Price", DoubleType),
      StructField("CPI", DoubleType),
      StructField("Unemployment", DoubleType)
    ))

    // Paths
    val featuresDatasetPath = "gs://walmart_recruiting_data_bucket/features.csv"
    val storesDatasetPath = "gs://walmart_recruiting_data_bucket/stores.csv"
    val enrichedDataOutputPath = "gs://walmart_recruiting_data_bucket/enriched_data/"
    val aggregatedSalesOutputPath = "gs://walmart_recruiting_data_bucket/aggregated_sales/"
    val checkpointPath = "gs://walmart_recruiting_data_bucket/checkpoints/"

    // Kafka Data Processing
    val salesStream = kafkaStream.selectExpr("CAST(value AS STRING) AS jsonString")
      .select(from_json(col("jsonString"), salesSchema).as("data"))
      .select("data.*")

    // Additional Datasets
    val featuresDf = spark.read.option("header", "true").schema(featuresSchema).csv(featuresDatasetPath)
    val storesDf = spark.read.option("header", "true").schema(storesSchema).csv(storesDatasetPath)

    // Data Cleaning
    val cleanedFeaturesDf = featuresDf.na.drop().cache()
    val cleanedStoresDf = broadcast(storesDf.na.drop())

    val enrichedStream = salesStream
      .withColumn("Date", to_date(col("Date"), "yyyy-MM-dd"))
      .join(cleanedFeaturesDf, Seq("Store", "Date"), "inner")
      .join(cleanedStoresDf, Seq("Store"), "inner")

    // Write enriched data to GCS
    enrichedStream.writeStream
      .outputMode("append")
      .format("parquet")
      .option("path", enrichedDataOutputPath)
      .option("checkpointLocation", checkpointPath + "/enriched_data/")
      .start()

    // Aggregations
    val storeMetrics = enrichedStream
      .groupBy("Store")
      .agg(
        sum("Weekly_Sales").as("Total_Weekly_Sales"),
        avg("Weekly_Sales").as("Avg_Weekly_Sales")
      )

    val deptMetrics = enrichedStream
      .groupBy("Dept", "Date")
      .agg(
        sum("Weekly_Sales").as("Total_Weekly_Sales")
      )

    val holidayMetrics = enrichedStream
      .groupBy("Dept", "IsHoliday")
      .agg(
        sum("Weekly_Sales").as("Total_Weekly_Sales"),
        avg("Weekly_Sales").as("Average_Weekly_Sales")
      )
      .withColumn("Day_Type", when(col("IsHoliday"), "Holiday").otherwise("Workday"))
      .drop("IsHoliday")

    // Write metrics to GCS
    storeMetrics.writeStream
      .outputMode("complete")
      .format("json")
      .option("path", aggregatedSalesOutputPath + "/store_metrics/")
      .option("checkpointLocation", checkpointPath + "/store_metrics/")
      .start()

    deptMetrics.writeStream
      .outputMode("complete")
      .format("json")
      .option("path", aggregatedSalesOutputPath + "/dept_metrics/")
      .option("checkpointLocation", checkpointPath + "/dept_metrics/")
      .start()

    holidayMetrics.writeStream
      .outputMode("complete")
      .format("json")
      .option("path", aggregatedSalesOutputPath + "/holiday_metrics/")
      .option("checkpointLocation", checkpointPath + "/holiday_metrics/")
      .start()

    spark.streams.awaitAnyTermination()
  }
}
