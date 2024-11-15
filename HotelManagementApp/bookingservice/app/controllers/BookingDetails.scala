package controllers

import play.api.libs.json._

import scala.collection.mutable.ListBuffer

// Define Room as a case class

case class Room(roomNumber: String, capacity: Int, type: RoomType)
object Room {
  implicit val roomFormat: OFormat[Room] = Json.format[Room]
}

// Define GuestDetails as a case class
case class GuestDetails(name: String, email: String)
object GuestDetails {
  implicit val guestDetailsFormat: OFormat[GuestDetails] = Json.format[GuestDetails]
}

// Define BookingDetails as a case class
case class BookingDetails(booking_id: String, guestDetails: GuestDetails, bookings: ListBuffer[BookingTiming], room: Room)
object BookingDetails {
  implicit val bookingDetailsFormat: OFormat[BookingDetails] = Json.format[BookingDetails]
}
