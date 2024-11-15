package models

import spray.json.DefaultJsonProtocol._
import spray.json._

case class GuestInfo(recipient: String, subject: String, message: String)

object JsonFormats {
  implicit val guestFormat: RootJsonFormat[GuestInfo] = jsonFormat3(GuestInfo)
}

