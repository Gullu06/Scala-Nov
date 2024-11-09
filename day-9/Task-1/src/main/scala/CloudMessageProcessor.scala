import akka.actor.Actor
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class CloudMessageProcessor(producer: KafkaProducer[String, String]) extends Actor {
  override def receive: Receive = {
    case msg: Message =>
      val record = new ProducerRecord[String, String]("cloud-message", msg.messageKey, msg.message)
      producer.send(record)
      println(s"CloudMessageProcessor sent: ${msg.message}")
  }
}
