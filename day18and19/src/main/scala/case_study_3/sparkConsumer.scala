package case_study_3

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object sparkConsumer {
  def main(args: Array[String]): Unit = {
    // Initialize Spark session
    val sparkSession = SparkSession.builder()
      .appName("Kafka Structured Streaming Application")
      .master("local[*]")
      .getOrCreate()

    sparkSession.sparkContext.setLogLevel("ERROR") // Reduce verbosity in logs

    // Schema definition for transaction messages
    val transactionSchema = new StructType()
      .add("transactionId", StringType)
      .add("userId", StringType)
      .add("amount", IntegerType)

    // Read the Kafka stream
    val kafkaSource = sparkSession.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "transactions")
      .option("startingOffsets", "latest")
      .load()

    // Parse JSON messages from Kafka
    val transactionData = kafkaSource
      .selectExpr("CAST(value AS STRING) AS jsonString")
      .select(from_json(col("jsonString"), transactionSchema).as("parsed"))
      .select("parsed.transactionId", "parsed.amount", "parsed.userId")

    // Add a timestamp column to the transaction data
    val enrichedTransactions = transactionData.withColumn("eventTime", current_timestamp())

    // Perform windowed aggregation to calculate the total transaction amount
    val aggregatedData = enrichedTransactions
      .withWatermark("eventTime", "10 seconds")
      .groupBy(window(col("eventTime"), "10 seconds"))
      .agg(sum("amount").as("totalTransactionAmount"))

    // Write aggregated results to the console
    val consoleOutput = aggregatedData.writeStream
      .outputMode("update") // Updates for each window
      .format("console")
      .option("truncate", "false") // Prevent truncation of output
      .start()

    consoleOutput.awaitTermination() // Keep the application running
  }
}

