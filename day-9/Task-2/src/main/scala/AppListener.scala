import akka.actor.{Actor, ActorRef}
import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.jdk.CollectionConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AppListener(messageGatherer: ActorRef) extends Actor {
  val consumer = new KafkaConsumer[String, String](KafkaConsumerFactory.createConsumerProps("app-group"))
  consumer.subscribe(java.util.Collections.singletonList("app-message"))

  override def preStart(): Unit = {
    Future {
      try {
        while (true) {
          val records = consumer.poll(java.time.Duration.ofMillis(100)).asScala
          for (record <- records) {
            messageGatherer ! Message("AppMessage", record.value(), record.key())
          }
        }
      } catch {
        case e: Exception =>
          println(s"Error in AppListener polling loop: ${e.getMessage}")
      }
    }
  }

  override def postStop(): Unit = {
    consumer.close()
    super.postStop()
  }

  override def receive: Receive = Actor.emptyBehavior
}
