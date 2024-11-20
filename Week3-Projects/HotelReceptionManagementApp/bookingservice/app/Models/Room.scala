package models

import play.api.libs.json.{Json, OFormat}

case class Room(
                 room_id: Int,
                 room_no: Int,
                 floor_no: Int,
                 room_type: String,
                 room_status: String,
                 price: Double
               )

object Room {
  implicit val format: OFormat[Room] = Json.format[Room]
}
