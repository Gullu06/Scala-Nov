package producers

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.sql.SparkSession
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.duration._
import scala.util.Random

object MoviesMetadataProducer {
  def main(args: Array[String]): Unit = {
    val topicName = "movies-metadata"

    // Kafka producer properties
    val producerProps = new java.util.Properties()
    producerProps.put("bootstrap.servers", "localhost:9092")
    producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val kafkaProducer = new KafkaProducer[String, String](producerProps)

    // Initialize Spark session
    val spark = SparkSession.builder()
      .appName("Movies Metadata Producer")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    // Read movies dataset from GCS
    val moviesDF = spark.read
      .option("header", "true")
      .csv("gs://movie_bucket_pc/movie.csv")

    // Shuffle rows for random selection
    val random = new Random()
    val movies = moviesDF.collect().toIndexedSeq

    try {
      while (true) {
        // Randomly select a row to simulate real-time data
        val row = movies(random.nextInt(movies.length))

        val movieId = row.getAs[String]("movieId")
        val title = row.getAs[String]("title")
        val genres = row.getAs[String]("genres")

        // Build JSON message
        val movieJson = ("movieId" -> movieId) ~
          ("title" -> title) ~
          ("genres" -> genres)

        val movieMessage = compact(render(movieJson))

        // Send the message to Kafka
        val kafkaRecord = new ProducerRecord[String, String](topicName, movieId, movieMessage)
        kafkaProducer.send(kafkaRecord)

        println(s"Produced movie metadata message: $movieMessage")

        // Simulate real-time delay
        Thread.sleep(100) // Adjust delay as needed
      }
    } finally {
      kafkaProducer.close()
    }
  }
}
