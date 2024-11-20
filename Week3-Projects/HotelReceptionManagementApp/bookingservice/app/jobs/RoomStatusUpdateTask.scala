package jobs

import javax.inject._
import play.api.Logging
import repositories.{BookingDetailsRepository, GuestRepository, RoomRepository}
import scala.concurrent.{ExecutionContext, Future}
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import java.time.LocalDate

@Singleton
class RoomStatusUpdateTask @Inject()(
                                      bookingDetailsRepository: BookingDetailsRepository,
                                      roomRepository: RoomRepository,
                                      guestRepository: GuestRepository
                                    )(implicit ec: ExecutionContext) extends Logging {

  private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

  // Schedule the cron job to run daily at midnight
  scheduler.scheduleAtFixedRate(() => {
    Future(checkAndUpdateRoomAndGuestStatus()).recover {
      case ex: Exception => logger.error("Scheduled task execution failed", ex)
    }
  }, 0, 1, TimeUnit.DAYS)

  /** Shutdown the scheduler on application stop */
  sys.addShutdownHook {
    logger.info("Shutting down scheduler for RoomStatusUpdateTask")
    scheduler.shutdown()
    if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
      scheduler.shutdownNow()
    }
  }

  /** Check and update room and guest statuses */
  def checkAndUpdateRoomAndGuestStatus(): Future[Unit] = {
    val today = LocalDate.now()
    logger.info(s"Cron job started: Checking room and guest statuses for $today")

    bookingDetailsRepository.getBookingsEndingOn(today).flatMap { bookingsEndingToday =>
      Future.sequence(bookingsEndingToday.map(updateRoomAndGuestStatus)).map { _ =>
        logger.info("Cron job completed: Room and guest statuses updated for bookings ending today.")
      }
    }.recover { case ex =>
      logger.error("Cron job failed", ex)
    }
  }

  /** Update room and guest statuses for a specific booking */
  private def updateRoomAndGuestStatus(booking: models.BookingDetails): Future[Unit] = {
    for {
      // Fetch the room number for the booking
      roomNoOption <- roomRepository.getRoomNoById(booking.room_id)

      // Update guest statuses if room number is found
      _ <- roomNoOption match {
        case Some(room_no) =>
          logger.info(s"Updating guest statuses to INACTIVE for room number: $room_no")
          guestRepository.updateGuestsStatusByRoomNo(room_no, "INACTIVE")
        case None =>
          logger.warn(s"No room number found for room ID: ${booking.room_id}")
          Future.successful(())
      }

      // Update the room status to AVAILABLE
      _ <- {
        logger.info(s"Updating room status to AVAILABLE for room ID: ${booking.room_id}")
        roomRepository.updateRoomStatusById(booking.room_id, "AVAILABLE")
      }
    } yield ()
  }
}
