package controllers

import javax.inject._
import play.api.mvc._
import jobs.RoomStatusUpdateTask
import repositories.{BookingDetailsRepository, GuestRepository, RoomRepository}
import services.KafkaProducerService
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.{JsValue, Json, Reads}
import models.{BookingDetails, Guest}
import play.api.Logging
import java.util.Base64
import java.time.LocalDate

@Singleton
class RoomController @Inject()(
                                val controllerComponents: ControllerComponents,
                                roomStatusUpdateTask: RoomStatusUpdateTask,
                                roomRepository: RoomRepository,
                                guestRepository: GuestRepository,
                                bookingDetailsRepository: BookingDetailsRepository,
                                kafkaProducerService: KafkaProducerService
                              )(implicit ec: ExecutionContext) extends BaseController with Logging {

  /** Trigger manual execution of the room status update task */
  def triggerRoomStatusUpdate: Action[AnyContent] = Action.async {
    roomStatusUpdateTask.checkAndUpdateRoomAndGuestStatus().map { _ =>
      Ok(Json.obj("message" -> "Cron job triggered manually"))
    }.recover { case ex =>
      logger.error("Error triggering room status update task", ex)
      InternalServerError(Json.obj("message" -> "Failed to trigger cron job"))
    }
  }

  /** API to fetch available rooms by type */
  def getAvailableRoomsByType(room_type: String): Action[AnyContent] = Action.async {
    roomRepository.getAvailableRoomsByType(room_type).map { rooms =>
      Ok(Json.toJson(rooms))
    }.recover { case ex =>
      logger.error(s"Error fetching available rooms of type $room_type", ex)
      InternalServerError(Json.obj("message" -> "Failed to fetch available rooms"))
    }
  }

  // Case classes and implicits for JSON input
  case class RoomCheckoutRequest(room_no: Int)
  implicit val roomCheckoutRequestReads: Reads[RoomCheckoutRequest] = Json.reads[RoomCheckoutRequest]

  case class GuestData(
                        name: String,
                        email: String,
                        address: String,
                        id_proof: String,
                        guest_status: String
                      )
  case class GuestAllocationRequest(
                                     room_no: Int,
                                     guests: Seq[GuestData],
                                     end_date: LocalDate
                                   )

  implicit val guestDataReads: Reads[GuestData] = Json.reads[GuestData]
  implicit val guestAllocationRequestReads: Reads[GuestAllocationRequest] = Json.reads[GuestAllocationRequest]

  /** API to allocate a room to guests */
  def allocateRoom: Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[GuestAllocationRequest].fold(
      errors => {
        val errorMessages = errors.map { case (path, validationErrors) =>
          s"${path.toString()}: ${validationErrors.map(_.message).mkString(", ")}"
        }.mkString("; ")
        logger.warn(s"Invalid allocation request: $errorMessages")
        Future.successful(BadRequest(Json.obj("message" -> "Invalid data", "errors" -> errorMessages)))
      },
      allocationRequest => {
        if (allocationRequest.guests.size > 3) {
          Future.successful(BadRequest(Json.obj("message" -> "Only up to 3 guests allowed per room")))
        } else {
          val guestsWithRoomNo = allocationRequest.guests.map { guestData =>
            val idProofBytes = Base64.getDecoder.decode(guestData.id_proof)
            Guest(0, guestData.name, allocationRequest.room_no, guestData.email, guestData.address, idProofBytes, guestData.guest_status)
          }

          logger.info(s"Allocating room ${allocationRequest.room_no} to ${allocationRequest.guests.size} guests.")

          roomRepository.getRoomIdByRoomNo(allocationRequest.room_no).flatMap {
            case Some(room_id) =>
              val transaction = for {
                guestIds <- guestRepository.addGuestsAndReturnIds(guestsWithRoomNo)
                _ <- roomRepository.updateRoomStatusByRoomNo(allocationRequest.room_no, "OCCUPIED")
                _ <- bookingDetailsRepository.addBooking(BookingDetails(
                  booking_id = 0,
                  guest_id = guestIds.head,
                  room_id = room_id,
                  start_date = LocalDate.now(),
                  end_date = allocationRequest.end_date
                ))
              } yield guestIds

              transaction.flatMap { guestIds =>
                val kafkaFutures = allocationRequest.guests.map { guest =>
                  kafkaProducerService.sendGuestBookingMessage(guest.name, guest.email)
                }

                Future.sequence(kafkaFutures).map { _ =>
                  logger.info(s"Successfully allocated room ${allocationRequest.room_no}")
                  Ok(Json.obj("message" -> "Room allocated successfully", "guest_ids" -> guestIds))
                }
              }.recover { case ex =>
                logger.error(s"Error allocating room ${allocationRequest.room_no}", ex)
                InternalServerError(Json.obj("message" -> "Room allocation failed"))
              }

            case None =>
              logger.warn(s"Invalid room number: ${allocationRequest.room_no}")
              Future.successful(BadRequest(Json.obj("message" -> "Invalid room number")))
          }
        }
      }
    )
  }

  /** API to check out guests by room number */
  def checkoutGuest: Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[RoomCheckoutRequest].fold(
      errors => {
        val errorMessages = errors.map { case (path, validationErrors) =>
          s"${path.toString()}: ${validationErrors.map(_.message).mkString(", ")}"
        }.mkString("; ")
        logger.warn(s"Invalid checkout request: $errorMessages")
        Future.successful(BadRequest(Json.obj("message" -> "Invalid data", "errors" -> errorMessages)))
      },
      checkoutRequest => {
        val room_no = checkoutRequest.room_no
        logger.info(s"Checking out room number: $room_no")

        val checkoutTransaction = for {
          guests <- guestRepository.findGuestsByRoomNo(room_no)
          _ <- Future.sequence(guests.map(guest => guestRepository.updateGuestStatus(guest.guest_id, "INACTIVE")))
          _ <- roomRepository.updateRoomStatusByRoomNo(room_no, "AVAILABLE")
        } yield guests

        checkoutTransaction.map { guests =>
          logger.info(s"Room $room_no checked out successfully for ${guests.size} guests.")
          Ok(Json.obj("message" -> "Room checked out successfully", "guest_count" -> guests.size))
        }.recover { case ex =>
          logger.error(s"Error checking out room $room_no", ex)
          InternalServerError(Json.obj("message" -> "Room checkout failed"))
        }
      }
    )
  }
}
