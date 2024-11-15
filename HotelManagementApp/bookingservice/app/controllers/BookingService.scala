package services

import controllers._
import java.util.UUID
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class BookingService {
  private val bookings = mutable.Map[String, BookingDetails]()

  def createBooking(room: Room, guest: GuestDetails, timing: BookingTiming): BookingDetails = {
    val bookingId = UUID.randomUUID().toString
    val bookingDetails = BookingDetails(bookingId, guest, ListBuffer(timing), room)
//  Handle timing should not overlap for different rooms
    bookings += (bookingId -> bookingDetails) //For get request
    for(booking <- bookings) {
    println(s"Booking: $booking")
    }
    bookingDetails
  }

  def getBookingById(id: String): Option[BookingDetails] = bookings.get(id)
}
