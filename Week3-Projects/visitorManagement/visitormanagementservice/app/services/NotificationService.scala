package services

import models.Notification
import producers.NotificationProducer
import javax.inject.{Inject, Singleton}

@Singleton
class NotificationService @Inject()(producer: NotificationProducer) {

  def notifyHostEmployee(visitorName: String, hostEmail: String): Unit = {
    val notification = Notification(
      recipient = hostEmail,
      subject = "Visitor Arrival Notification",
      message = s"Visitor $visitorName has arrived."
    )
    producer.sendNotification("host-notifications", notification)
  }

  def notifyITSupport(visitorName: String, visitorEmail: String): Unit = {
    val notification = Notification(
            recipient = visitorEmail,
            subject = "Wi-Fi Access Details",
            message = s"Dear $visitorName, please use the provided Wi-Fi access details upon your arrival."
          )
    producer.sendNotification("it-support-notifications", notification)
  }

  def notifySecurity(visitorName: String): Unit = {
    val notification = Notification(
            recipient = "priyanshichouhanofficial@gmail.com",
            subject = "Visitor Entry Notification - Security",
            message = s"Visitor $visitorName has checked in and requires clearance."
          )
    producer.sendNotification("security-notifications", notification)
  }
}
