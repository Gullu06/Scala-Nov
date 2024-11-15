package producers

import models.Notification
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import play.api.libs.json.Json

import java.util.Properties
import javax.inject.Singleton

@Singleton
class NotificationProducer {

  // Initialize Kafka producer configuration
  private val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)

  private val producer = new KafkaProducer[String, String](props)

  def sendNotification(topic: String, notification: Notification): Unit = {
    val notificationJson = Json.toJson(notification).toString()
    val record = new ProducerRecord[String, String](topic, notificationJson)
    producer.send(record)
  }
}
