package models

import play.api.libs.json.{Json, OFormat}

case class Guest(
                  guest_id: Long,
                  name: String,
                  room_no: Int,
                  email: String,
                  address: String,
                  id_proof: Array[Byte], // For storing the image as binary data
                  guest_status: String
                )

object Guest {
  implicit val format: OFormat[Guest] = Json.format[Guest]
}
