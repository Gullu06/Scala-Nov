package case_study_5

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._

object SparkConsumer {
  def main(args: Array[String]): Unit = {
    // Initialize Spark session
    val spark = SparkSession.builder()
      .appName("Order Consumer with Broadcast Join")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "src/main/resources/spark-gcs-key.json")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    // GCS paths
    val userDetailsPath = "gs://priyanshi-bucket/cs5/input/user_details.csv"
    val outputPath = "gs://priyanshi-bucket/cs5/output/enriched_orders/"

    // Load user details dataset
    val userDetails = spark.read
      .option("header", "true")
      .csv(userDetailsPath)

    println("Broadcasted User Details Schema:")
    userDetails.printSchema()

    // Broadcast the user details
    val userDetailsBroadcast = broadcast(userDetails)

    // Kafka stream reading configuration
    val kafkaStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "orders")
      .load()

    // Define schema for Kafka messages
    val orderSchema = new StructType()
      .add("orderId", StringType)
      .add("userId", StringType)
      .add("orderAmount", DoubleType)

    // Deserialize and parse Kafka messages
    val ordersStream = kafkaStream.selectExpr("CAST(value AS STRING) AS jsonString")
      .select(from_json(col("jsonString"), orderSchema).as("data"))
      .select("data.*")

    // Enrich orders with user details using broadcast join
    val enrichedOrders = ordersStream
      .join(userDetailsBroadcast, Seq("userId"), "left")
      .select(
        col("orderId"),
        col("userId"),
        col("orderAmount"),
        col("name").alias("userName"),
        col("email").alias("userEmail")
      )

    // Write enriched orders to GCS in JSON format
    val query = enrichedOrders.writeStream
      .outputMode("append")
      .format("json")
      .option("path", outputPath)
      .option("jsonPath", "gs://priyanshi-bucket/cs5/jsonPath/")
      .trigger(Trigger.ProcessingTime("30 seconds"))
      .start()

    query.awaitTermination()
  }
}
