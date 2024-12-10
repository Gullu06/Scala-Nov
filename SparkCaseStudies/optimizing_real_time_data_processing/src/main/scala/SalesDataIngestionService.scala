import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._

object SalesDataIngestionService {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Real-Time Sales Consumer")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    val kafkaStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "weekly_sales")
      .option("startingOffsets", "latest")
      .load()

    val salesSchema = new StructType()
      .add("Store", StringType)
      .add("Dept", StringType)
      .add("Weekly_Sales", DoubleType)
      .add("Date", StringType)

    val salesStream = kafkaStream.selectExpr("CAST(value AS STRING) AS jsonString")
      .select(from_json(col("jsonString"), salesSchema).as("data"))
      .select("data.*")

    val aggregatedSales = salesStream
      .withColumn("Date", to_date(col("Date"), "yyyy-MM-dd"))
      .groupBy("Store", "Dept", "Date")
      .agg(
        sum("Weekly_Sales").as("Total_Weekly_Sales"),
        avg("Weekly_Sales").as("Average_Weekly_Sales")
      )

    val query = aggregatedSales.writeStream
      .outputMode("update")
      .format("parquet")
      .option("path", "gs://walmart_recruiting_data_bucket/aggregated_sales/")
      .option("checkpointLocation", "gs://walmart_recruiting_data_bucket/checkpoints/")
      .trigger(Trigger.ProcessingTime("30 seconds"))
      .start()

    query.awaitTermination()
  }
}
