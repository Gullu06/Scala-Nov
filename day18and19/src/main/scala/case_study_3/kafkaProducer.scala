package case_study_3

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import scala.util.Random

object kafkaProducer{
  def main(args: Array[String]): Unit = {
    // Kafka topic name
    val kafkaTopic = "transactions"

    // Producer configuration properties
    val producerProperties = new Properties()
    producerProperties.put("bootstrap.servers", "localhost:9092")
    producerProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    producerProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    // Create Kafka producer
    val kafkaProducer = new KafkaProducer[String, String](producerProperties)

    val randomGenerator = new Random()

    // Simulate a list of user IDs
    val userList = (1 to 10).map(i => f"user_${i}%02d")

    try {
      while (true) {
        // Generate random transaction data
        val transactionId = randomGenerator.alphanumeric.take(12).mkString
        val userId = userList(randomGenerator.nextInt(userList.length))
        val transactionAmount = randomGenerator.nextInt(5000) + 100 // Amount between 100 and 5100
        val transactionMessage =
          s"""
             |{
             |  "transactionId": "$transactionId",
             |  "userId": "$userId",
             |  "amount": $transactionAmount
             |}
             |""".stripMargin

        // Send the record to Kafka
        val kafkaRecord = new ProducerRecord[String, String](kafkaTopic, userId, transactionMessage)
        kafkaProducer.send(kafkaRecord)

        println(s"Message sent: $transactionMessage")
        Thread.sleep(1000) // Simulate a delay of 1 second between messages
      }
    } finally {
      kafkaProducer.close()
    }
  }
}
