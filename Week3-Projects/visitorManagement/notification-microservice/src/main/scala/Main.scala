import actors.MailSenderActorSystem.mailSenderActor
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import models.{Email, GuestInfo}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import models.JsonFormats._
import spray.json._

object Main {
  private def composeMail(guestInfo: GuestInfo): Email = {
    println(s"[Main] Composing email for: ${guestInfo.recipient}")
    Email(guestInfo.recipient, guestInfo.subject, guestInfo.message)
  }

  def main(args: Array[String]): Unit = {
    println("[Main] Starting application...")

    implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "notificationService")

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(sys.env.getOrElse("BROKER_HOST", "localhost") + ":9092")
      .withGroupId("notification_service_group") // Use unique group ID
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest") // Start reading from the latest message

    println("[Main] Consumer settings configured.")

    // Consumer for host-notifications
    consumeTopic("host-notifications", consumerSettings)

    // Consumer for it-support-notifications
    consumeTopic("it-support-notifications", consumerSettings)

    // Consumer for security-notifications
    consumeTopic("security-notifications", consumerSettings)
  }

  private def consumeTopic(topic: String, consumerSettings: ConsumerSettings[String, String])(implicit system: ActorSystem[_]): Unit = {
    Consumer
      .plainSource(consumerSettings, Subscriptions.topics(topic))
      .map(record => {
        println(s"[Main] Received raw message from $topic: ${record.value()}")
        val guestInfo = record.value().parseJson.convertTo[GuestInfo]
        println(s"[Main] Parsed message from $topic: $guestInfo")
        composeMail(guestInfo)
      })
      .runWith(Sink.foreach { email =>
        println(s"[Main] Sending email for $topic to actor: $email")
        mailSenderActor ! email
      })
  }
}
