import akka.actor.{Actor, ActorRef}
import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.jdk.CollectionConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CloudListener(messageGatherer: ActorRef) extends Actor {
  val consumer = new KafkaConsumer[String, String](KafkaConsumerFactory.createConsumerProps("cloud-group"))
  consumer.subscribe(java.util.Collections.singletonList("cloud-message"))

  override def preStart(): Unit = {
    // Start polling in a separate thread
    Future {
      try {
        while (true) {
          val records = consumer.poll(java.time.Duration.ofMillis(100)).asScala
          for (record <- records) {
            messageGatherer ! Message("CloudMessage", record.value(), record.key())
          }
        }
      } catch {
        case e: Exception =>
          println(s"Error in CloudListener polling loop: ${e.getMessage}")
      }
    }
  }

  // Close the consumer when the actor stops
  override def postStop(): Unit = {
    consumer.close()
    super.postStop()
  }

  override def receive: Receive = Actor.emptyBehavior
}
