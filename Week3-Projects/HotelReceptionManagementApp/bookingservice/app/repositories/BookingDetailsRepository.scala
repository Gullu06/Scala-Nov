package repositories

import models.BookingDetails
import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

class BookingDetailsRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  // BookingDetails table definition
  private class BookingDetailsTable(tag: Tag) extends Table[BookingDetails](tag, "booking_details") {
    def bookingId = column[Int]("booking_id", O.PrimaryKey, O.AutoInc) // Primary key
    def guestId = column[Long]("guest_id") // Foreign key reference to guests
    def roomId = column[Int]("room_id") // Foreign key reference to rooms
    def startDate = column[LocalDate]("start_date") // Booking start date
    def endDate = column[LocalDate]("end_date") // Booking end date

    // Mapping to BookingDetails model
    def * = (bookingId, guestId, roomId, startDate, endDate) <> ((BookingDetails.apply _).tupled, BookingDetails.unapply)
  }

  // Table query for BookingDetails
  private val bookingDetails = TableQuery[BookingDetailsTable]

  /** Find booking by ID */
  def findBookingById(bookingId: Int): Future[Option[BookingDetails]] = db.run {
    bookingDetails.filter(_.bookingId === bookingId).result.headOption
  }

  /** Get all bookings ending on a specific date */
  def getBookingsEndingOn(date: LocalDate): Future[Seq[BookingDetails]] = db.run {
    bookingDetails.filter(_.endDate === date).result
  }

  /** Delete a booking by ID */
  def deleteBookingById(bookingId: Int): Future[Int] = db.run {
    bookingDetails.filter(_.bookingId === bookingId).delete
  }

  /** Add a new booking */
  def addBooking(booking: BookingDetails): Future[Int] = db.run {
    (bookingDetails returning bookingDetails.map(_.bookingId)) += booking
  }
}
