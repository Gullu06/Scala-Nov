package models

import java.time.LocalDate
import play.api.libs.json.{Json, OFormat}

case class BookingDetails(
                           booking_id: Int,
                           guest_id: Long,
                           room_id: Int,
                           start_date: LocalDate,
                           end_date: LocalDate
                         )

object BookingDetails {
  implicit val format: OFormat[BookingDetails] = Json.format[BookingDetails]
}
