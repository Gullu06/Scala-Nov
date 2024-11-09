import akka.actor.Actor
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class AppMessageProcessor(producer: KafkaProducer[String, String]) extends Actor {
  override def receive: Receive = {
    case msg: Message =>
      println(s"AppMessageProcessor received: ${msg.message}")
      val record = new ProducerRecord[String, String]("app-message", msg.messageKey, msg.message)
      producer.send(record)
      println(s"AppMessageProcessor sent: ${msg.message}")
  }
}
