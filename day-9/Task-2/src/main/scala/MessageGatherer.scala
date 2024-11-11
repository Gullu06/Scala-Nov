import akka.actor.Actor
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}

class MessageGatherer(producer: KafkaProducer[String, String]) extends Actor {
  override def receive: Receive = {
    case msg: Message =>
      println(s"MessageGatherer received: ${msg.message} from ${msg.messageType}")
      val record = new ProducerRecord[String, String]("consolidated-messages", msg.messageKey, msg.message)

      // Asynchronously send the message with a callback to handle success/failure
      producer.send(record, (metadata: RecordMetadata, exception: Exception) => {
        if (exception != null) {
          println(s"Error sending message: ${exception.getMessage}")
        } else {
          println(s"Message sent to consolidated-messages topic with offset ${metadata.offset()}: ${msg.message}")
        }
      })
  }
}
