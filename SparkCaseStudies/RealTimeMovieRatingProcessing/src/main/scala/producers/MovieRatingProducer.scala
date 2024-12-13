package producers

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import java.time.Instant
import java.util.Properties
import scala.util.Random

object MovieRatingProducer {
  def main(args: Array[String]): Unit = {
    val topicName = "movie-ratings"

    val producerProps = new Properties()
    producerProps.put("bootstrap.servers", "localhost:9092")
    producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val kafkaProducer = new KafkaProducer[String, String](producerProps)
    val randomGenerator = new Random()

    val userIds = (1 to 400).map(_.toString)
    val movieIds = (1 to 27278).map(_.toString)

    try {
      while (true) {
        val randomUser = userIds(randomGenerator.nextInt(userIds.length))
        val randomMovie = movieIds(randomGenerator.nextInt(movieIds.length))
        val randomRating = (randomGenerator.nextInt(10) + 1) * 0.5
        val timestamp = Instant.now().toString

        val ratingJson = ("userId" -> randomUser) ~
          ("movieId" -> randomMovie) ~
          ("rating" -> randomRating) ~
          ("timestamp" -> timestamp)

        val ratingMessage = compact(render(ratingJson))
        kafkaProducer.send(new ProducerRecord[String, String](topicName, randomUser, ratingMessage))

        println(s"Produced rating message: $ratingMessage")
        Thread.sleep(50)
      }
    } finally {
      kafkaProducer.close()
    }
  }
}