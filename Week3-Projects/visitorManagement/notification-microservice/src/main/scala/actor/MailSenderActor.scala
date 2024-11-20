package actors

import akka.actor.{Actor, Props}
import models.Email

import java.util.Properties
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Authenticator, Message, PasswordAuthentication, Session, Transport}

class MailSenderActor extends Actor {
  def receive: Receive = {
    case email: Email =>
      println(s"[MailSenderActor] Received email to send: $email")
      sendEmail(email)
  }

  private def sendEmail(email: Email): Unit = {
    val properties: Properties = new Properties()
    properties.put("mail.smtp.auth", "true")
    properties.put("mail.smtp.starttls.enable", "true")
    properties.put("mail.smtp.host", "smtp.gmail.com")
    properties.put("mail.smtp.port", "587")
    properties.put("mail.smtp.ssl.trust", "smtp.gmail.com")
    properties.put("mail.smtp.ssl.protocols", "TLSv1.2")

    // Retrieve authentication details from environment variables
    val auth_mail = sys.env.get("auth_dets")
    val auth_pass = sys.env.get("pass")

    // Ensure environment variables are set
    if (auth_mail.isEmpty || auth_pass.isEmpty) {
      println("[MailSenderActor] Error: Missing environment variables 'auth_dets' or 'pass'.")
      throw new IllegalStateException("Missing required environment variables for email authentication.")
    }

    val session = Session.getInstance(properties, new Authenticator() {
      override protected def getPasswordAuthentication =
        new PasswordAuthentication(auth_mail.get, auth_pass.get)
    })

    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(auth_mail.get, "Visitor Management System")) // Sender's email
      message.setRecipient(Message.RecipientType.TO, new InternetAddress(email.email)) // Recipient's email
      message.setSubject(email.subject)
      message.setText(email.body)

      Transport.send(message)
      println(s"[MailSenderActor] Email sent to ${email.email}") // Confirmation logging
    } catch {
      case e: Exception =>
        println(s"[MailSenderActor] Failed to send email to ${email.email}: ${e.getMessage}")
        e.printStackTrace()
    }
  }
}

object MailSenderActorSystem {
  val system = akka.actor.ActorSystem("MailSenderActorSystem")
  val mailSenderActor = system.actorOf(Props[MailSenderActor], "MailSenderActor")
}
