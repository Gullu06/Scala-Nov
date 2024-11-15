package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.ExecutionContext
import services.BookingService
import play.api.libs.json._

@Singleton
class BookingController @Inject() (val controllerComponents: ControllerComponents, bookingService: BookingService)(implicit ec: ExecutionContext) extends BaseController {

  def getBooking(id: String) = Action {
    bookingService.getBookingById(id) match {
      case Some(bookingDetails) => Ok(Json.toJson(bookingDetails))
      case None => NotFound(Json.obj("error" -> "Booking not found"))
    }
  }

  def createBooking = Action(parse.json) { request =>
    request.body.validate[BookingDetailsRequest].map { bookingRequest =>
//      Get BookingDetailsRequest and add logic so that it should not overlap booking
//      val mapRoomsAndTiming = mutable.Map[Room, BookingTiming]()
      val bookingDetails = bookingService.createBooking(bookingRequest.room, bookingRequest.guest, bookingRequest.timing)
      Created(Json.toJson(bookingDetails))
    }.getOrElse {
      BadRequest(Json.obj("error" -> "Invalid booking details"))
    }
  }
}

// JSON format definitions for the request class
case class BookingDetailsRequest(room: Room, guest: GuestDetails, timing: BookingTiming)
object BookingDetailsRequest {
  implicit val bookingDetailsRequestFormat: OFormat[BookingDetailsRequest] = Json.format[BookingDetailsRequest]
}
