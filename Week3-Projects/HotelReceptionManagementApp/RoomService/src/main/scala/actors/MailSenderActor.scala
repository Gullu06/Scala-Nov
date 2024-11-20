package actors

import akka.actor.{Actor, ActorSystem, Props}
import models.Email
import org.slf4j.LoggerFactory

import java.util.Properties
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Authenticator, Message, MessagingException, PasswordAuthentication, Session, Transport}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class MailSenderActor(implicit ec: ExecutionContext) extends Actor {
  private val logger = LoggerFactory.getLogger(getClass)

  def receive: Receive = {
    case email: Email =>
      logger.info(s"Received email request to send to ${email.receiverId}")
      Future {
        sendEmail(email)
      }.onComplete {
        case Success(_) => logger.info(s"Email sent successfully to ${email.receiverId}")
        case Failure(ex) => logger.error(s"Failed to send email to ${email.receiverId}", ex)
      }
  }

  private def sendEmail(email: Email): Unit = {
    val properties: Properties = new Properties()
    properties.put("mail.smtp.host", "smtp.gmail.com")
    properties.put("mail.smtp.port", "587")
    properties.put("mail.smtp.auth", "true")
    properties.put("mail.smtp.starttls.enable", "true")

    // Retrieve authentication details from environment variables
    val authenticationMail = sys.env.getOrElse("AUTH_EMAIL", throw new IllegalArgumentException("AUTH_EMAIL is not set"))
    val authenticationPass = sys.env.getOrElse("AUTH_PASS", throw new IllegalArgumentException("AUTH_PASS is not set"))

    val session = Session.getInstance(properties, new Authenticator() {
      override protected def getPasswordAuthentication: PasswordAuthentication =
        new PasswordAuthentication(authenticationMail, authenticationPass)
    })

    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(authenticationMail))
      message.setRecipients(Message.RecipientType.TO, email.receiverId)
      message.setSubject(email.subject)
      message.setText(email.body)

      Transport.send(message)
      logger.info(s"Email sent to ${email.receiverId}")
    } catch {
      case e: MessagingException =>
        logger.error(s"Failed to send email to ${email.receiverId}: ${e.getMessage}", e)
        throw e
    }
  }
}

object MailSenderActorSystem {
  implicit val system: ActorSystem = ActorSystem("MailSenderActorSystem")
  implicit val executionContext: ExecutionContext = system.dispatcher

  val mailSenderActor = system.actorOf(Props(new MailSenderActor), "MailSenderActor")
}
