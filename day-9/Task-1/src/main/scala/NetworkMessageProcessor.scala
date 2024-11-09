import akka.actor.Actor
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class NetworkMessageProcessor(producer: KafkaProducer[String, String]) extends Actor {
  override def receive: Receive = {
    case msg: Message =>
      println(s"NetworkMessageProcessor received message: ${msg.message} with key ${msg.messageKey}")
      val record = new ProducerRecord[String, String]("network-message", msg.messageKey, msg.message)
      producer.send(record)
      println(s"NetworkMessageProcessor sent message to Kafka topic 'network-message'")
  }
}
