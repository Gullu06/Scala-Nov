package controllers

import javax.inject._
import play.api.mvc._
import jobs.RoomStatusUpdateTask
import repositories.{BookingDetailsRepository, GuestRepository, RoomRepository}
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json
import models.{BookingDetails, Guest}

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.{JsValue, Json, Reads}

import java.util.Base64
import play.api.Logging
import play.api.i18n.Lang.logger

import java.time.LocalDate

@Singleton
class RoomController @Inject()(
                                val controllerComponents: ControllerComponents,
                                roomStatusUpdateTask: RoomStatusUpdateTask,
                                roomRepository: RoomRepository,
                                guestRepository: GuestRepository,
                                bookingDetailsRepository: BookingDetailsRepository
                              )(implicit ec: ExecutionContext) extends BaseController {

  def triggerRoomStatusUpdate: Action[AnyContent] = Action.async {
    roomStatusUpdateTask.checkAndUpdateRoomAndGuestStatus().map { _ =>
      Ok(Json.obj("message" -> "Cron job triggered manually"))
    }
  }


  // API to get available rooms by type
    def getAvailableRoomsByType(roomType: String): Action[AnyContent] = Action.async {
      roomRepository.getAvailableRoomsByType(roomType).map { rooms =>
        Ok(Json.toJson(rooms))
      }
    }

    case class RoomCheckoutRequest(roomNo: Int)
    implicit val roomCheckoutRequestReads: Reads[RoomCheckoutRequest] = Json.reads[RoomCheckoutRequest]


    case class GuestData(
                          name: String,
                          email: String,
                          address: String,
                          idProof: String,
                          guestStatus: String
                        )

    case class GuestAllocationRequest(
                                       roomNo: Int,
                                       guests: Seq[GuestData],
                                       endDate: LocalDate
                                     )

    implicit val guestDataReads: Reads[GuestData] = Json.reads[GuestData]
    implicit val guestAllocationRequestReads: Reads[GuestAllocationRequest] = Json.reads[GuestAllocationRequest]


    // API to allocate a room to guests
    def allocateRoom: Action[JsValue] = Action.async(parse.json) { request =>
      request.body.validate[GuestAllocationRequest].fold(
        errors => {
          val errorMessages = errors.map { case (path, validationErrors) =>
            s"${path.toString()}: ${validationErrors.map(_.message).mkString(", ")}"
          }
          Future.successful(BadRequest(Json.obj("message" -> "Invalid data", "errors" -> errorMessages)))
        },
        allocationRequest => {
          if (allocationRequest.guests.size > 3) {
            Future.successful(BadRequest(Json.obj("message" -> "Only up to 3 guests allowed per room")))
          } else {
            val guestsWithRoomNo = allocationRequest.guests.map { guestData =>
              val idProofBytes = Base64.getDecoder.decode(guestData.idProof)
              Guest(0, guestData.name, allocationRequest.roomNo, guestData.email, guestData.address, idProofBytes, guestData.guestStatus)
            }

            logger.info(s"Allocating room number: ${allocationRequest.roomNo} to ${allocationRequest.guests.size} guests.")

            // Step 1: Retrieve the actual RoomID from the Room table based on roomNo
            roomRepository.getRoomIdByRoomNo(allocationRequest.roomNo).flatMap {
              case Some(roomId) =>
                // Step 2: Proceed with the transaction only if RoomID exists
                val insertGuestsAndUpdateRoomAndBooking = for {
                  guestIds <- guestRepository.addGuestsAndReturnIds(guestsWithRoomNo) // Insert guests and retrieve IDs
                  _ <- roomRepository.updateRoomStatusByRoomNo(allocationRequest.roomNo, "OCCUPIED") // Update room status
                  _ <- bookingDetailsRepository.addBooking(BookingDetails(
                    bookingId = 0, // Will be auto-generated
                    guestId = guestIds.head, // Reference the first generated guest ID
                    roomId = roomId, // Use the actual RoomID instead of roomNo
                    startDate = LocalDate.now(), // Set start_date to the current date
                    endDate = allocationRequest.endDate // Use provided end_date
                  ))
                } yield ()

                insertGuestsAndUpdateRoomAndBooking.map { _ =>
                  logger.info(s"Successfully allocated room number: ${allocationRequest.roomNo}.")
                  Ok(Json.obj("message" -> "Room allocated successfully"))
                }.recover {
                  case ex: Exception =>
                    logger.error(s"Failed to allocate room number: ${allocationRequest.roomNo}", ex)
                    InternalServerError(Json.obj("message" -> "Failed to allocate room"))
                }

              case None =>
                // If roomNo doesn't correspond to any RoomID in the database, return an error
                Future.successful(BadRequest(Json.obj("message" -> "Invalid room number")))
            }
          }
        }
      )
    }

    // API to check out a guest by booking ID
    def checkoutGuest: Action[JsValue] = Action.async(parse.json) { request =>
      request.body.validate[RoomCheckoutRequest].fold(
        errors => {
          val errorMessages = errors.map { case (path, validationErrors) =>
            s"${path.toString()}: ${validationErrors.map(_.message).mkString(", ")}"
          }
          Future.successful(BadRequest(Json.obj("message" -> "Invalid data", "errors" -> errorMessages)))
        },
        checkoutRequest => {
          val roomNo = checkoutRequest.roomNo

          for {
            // Step 1: Retrieve all guests for the specified room number
            guests <- guestRepository.findGuestsByRoomNo(roomNo)

            // Step 2: Update guestStatus to "INACTIVE" for all guests in the room
            _ <- Future.sequence(guests.map(guest => guestRepository.updateGuestStatus(guest.guestId, "INACTIVE")))

            // Step 3: Update room status to "AVAILABLE"
            _ <- roomRepository.updateRoomStatusByRoomNo(roomNo, "AVAILABLE")

          } yield Ok(Json.obj("message" -> "Room checked out successfully"))
        }
      )
    }
}
