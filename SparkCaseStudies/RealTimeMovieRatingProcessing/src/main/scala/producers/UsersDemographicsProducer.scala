package producers

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.sql.SparkSession
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import scala.util.Random

object UsersDemographicsProducer {
  def main(args: Array[String]): Unit = {
    val topicName = "users-demographics"

    // Kafka producer properties
    val producerProps = new java.util.Properties()
    producerProps.put("bootstrap.servers", "localhost:9092")
    producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val kafkaProducer = new KafkaProducer[String, String](producerProps)

    // Initialize Spark session
    val spark = SparkSession.builder()
      .appName("Users Demographics Producer")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    // Read user demographics dataset from GCS
    val usersDF = spark.read
      .option("header", "true")
      .csv("gs://movie_bucket_pc/user_data.csv/")

    val users = usersDF.collect().toIndexedSeq
    val random = new Random()

    try {
      while (true) {
        // Randomly select a row to simulate real-time data
        val row = users(random.nextInt(users.length))

        val userId = row.getAs[String]("userId")
        val age = row.getAs[String]("age")
        val gender = row.getAs[String]("gender")
        val location = row.getAs[String]("location")

        // Build JSON message
        val userJson = ("userId" -> userId) ~
          ("age" -> age) ~
          ("gender" -> gender) ~
          ("location" -> location)

        val userMessage = compact(render(userJson))

        // Send the message to Kafka
        val kafkaRecord = new ProducerRecord[String, String](topicName, userId, userMessage)
        kafkaProducer.send(kafkaRecord)

        println(s"Produced user demographic message: $userMessage")

        // Simulate real-time delay
        Thread.sleep(100) // Adjust delay as needed
      }
    } finally {
      kafkaProducer.close()
    }
  }
}
