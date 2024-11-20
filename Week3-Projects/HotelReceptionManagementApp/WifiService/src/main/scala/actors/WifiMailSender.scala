//package actors
//
//import akka.actor.{Actor, ActorSystem, Props}
//import models.Email
//
//import java.util.Properties
//import javax.mail.{Authenticator, Message, MessagingException, PasswordAuthentication, Session, Transport}
//import javax.mail.internet.{InternetAddress, MimeMessage}
//
//class WifiMailSender extends Actor {
//  def receive: Receive = {
//    case email: Email => sendEmail(email)
//  }
//  private def sendEmail(email: Email): Unit = {
//    val properties: Properties = new Properties()
//    properties.put("mail.smtp.host", "smtp.gmail.com") // Replace with your SMTP server
//    properties.put("mail.smtp.port", "587")
//    properties.put("mail.smtp.auth", "true")
//    properties.put("mail.smtp.starttls.enable", "true")
//
//    val authenticationMail = sys.env.get("auth_dets").getOrElse("default_value")
//    val authenticationPass = sys.env.get("pass").getOrElse("default_value")
//
//    val session = Session.getInstance(properties, new Authenticator() {
//      override protected def getPasswordAuthentication =
//        new PasswordAuthentication("priyanshichouhan2908@gmail.com", "imvv uvhc njwx wvdc")
//    })
//    try {
//      val message = new MimeMessage(session)
//      message.setFrom(new InternetAddress("priyanshichouhan2908@gmail.com"))
//      message.setRecipients(Message.RecipientType.TO, email.receiverId)
//      message.setSubject(email.subject)
//      message.setText(email.body)
//      Transport.send(message)
//      println(s"Email sent to ${email.receiverId}")
//    } catch {
//      case e: MessagingException =>
//        e.printStackTrace()
//    }
//  }
//}
//
//
//object MailSenderActorSystem {
//  val system = ActorSystem("WifiMailSenderSystem")
//  val wifiMailSender = system.actorOf(Props[WifiMailSender], "WifiMailSender")
//}


//
//
//
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
    // Load SMTP properties
    val properties: Properties = new Properties()
    properties.put("mail.smtp.host", "smtp.gmail.com")
    properties.put("mail.smtp.port", "587")
    properties.put("mail.smtp.auth", "true")
    properties.put("mail.smtp.starttls.enable", "true")

    // Load authentication details from environment variables
    val authenticationMail = sys.env.get("auth_dets").getOrElse("priyanshichouhan2908@gmail.com")
    val authenticationPass = sys.env.get("pass").getOrElse("imvv uvhc njwx wvdc")

    // Initialize the mail session
    val session = Session.getInstance(properties, new Authenticator() {
      override protected def getPasswordAuthentication =
        new PasswordAuthentication(authenticationMail, authenticationPass)
    })

    try {
      // Create the email message
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(authenticationMail))
      message.setRecipients(Message.RecipientType.TO, email.receiverId) // Set recipient email
      message.setSubject(email.subject) // Set email subject
      message.setText(email.body) // Set email body
      Transport.send(message) // Send the email
      println(s"Email sent to ${email.receiverId}")
    } catch {
      case e: MessagingException =>
        e.printStackTrace() // Log the error
    }
  }
}

object MailSenderActorSystem {
  val system = ActorSystem("WifiMailSenderSystem")
  val wifiMailSender = system.actorOf(Props[WifiMailSender], "WifiMailSender")
}
