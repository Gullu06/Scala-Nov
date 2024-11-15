package controllers

import play.api.libs.json.{Json, OFormat}

import java.time.{LocalDate, LocalTime}

case class BookingTiming(var start_date: LocalDate, var end_date: LocalDate, var start_time: LocalTime, var end_time: LocalTime) {
  def overlaps(other: BookingTiming): Boolean = {
    // Check if two bookings overlap
    !((end_date.isBefore(other.start_date) || start_date.isAfter(other.end_date)) ||
      (end_time.isBefore(other.start_time) || start_time.isAfter(other.end_time)))
  }
}

//case class BookingTiming(start_date: LocalDate, end_date: LocalDate, start_time: LocalTime, end_time: LocalTime)
object BookingTiming {
  implicit val bookingTimingFormat: OFormat[BookingTiming] = Json.format[BookingTiming]
}
