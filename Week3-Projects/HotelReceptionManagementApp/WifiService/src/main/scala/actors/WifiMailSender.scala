package actors
import akka.actor.{Actor, ActorSystem, Props}
import models.Email
import java.util.Properties
import javax.mail.{Authenticator, Message, MessagingException, PasswordAuthentication, Session, Transport}
import javax.mail.internet.{InternetAddress, MimeMessage}
class WifiMailSender extends Actor {
  def receive: Receive = {
    case email: Email => sendEmail(email)
  }
  private def sendEmail(email: Email): Unit = {
    val properties: Properties = new Properties()
    properties.put("mail.smtp.host", "smtp.gmail.com")
    properties.put("mail.smtp.port", "587")
    properties.put("mail.smtp.auth", "true")
    properties.put("mail.smtp.starttls.enable", "true")
    // Retrieve authentication details from environment variable
    val authenticationMail = sys.env.getOrElse("AUTH_EMAIL", throw new IllegalArgumentException("AUTH_EMAIL is not set"))
    val authenticationPass = sys.env.getOrElse("AUTH_PASS", throw new IllegalArgumentException("AUTH_PASS is not set"))
    val session = Session.getInstance(properties, new Authenticator() {
      override protected def getPasswordAuthentication: PasswordAuthentication =
        new PasswordAuthentication(authenticationMail, authenticationPass)
    })
    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress("priyanshichouhan2908@gmail.com"))
      message.setRecipients(Message.RecipientType.TO, email.receiverId)
      message.setSubject(email.subject)
      message.setText(email.body)
      Transport.send(message)
      println(s"Email sent to ${email.receiverId}")
    } catch {
      case e: MessagingException =>
        e.printStackTrace()
    }
  }
}
object MailSenderActorSystem {
  val system = ActorSystem("WifiMailSenderSystem")
  val wifiMailSender = system.actorOf(Props[WifiMailSender], "WifiMailSender")
}