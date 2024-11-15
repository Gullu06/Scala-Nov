package models

import play.api.libs.json.{Json, OFormat}

case class Notification(
                         recipient: String,
                         subject: String,
                         message: String
                       )

object Notification {
  implicit val format: OFormat[Notification] = Json.format[Notification]
}
