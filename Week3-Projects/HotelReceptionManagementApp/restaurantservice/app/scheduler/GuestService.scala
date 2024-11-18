package scheduler

import models.{GuestDao, MenuDAO}
import utils.MailUtil.composeAndSendEmailAllGuests

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class GuestService @Inject()(guestRepository: GuestDao, menuDao: MenuDAO)(implicit ec: ExecutionContext) {

  def fetchGuestListAndSendMenu(): Unit = {
    menuDao.list().flatMap { menuList =>
      guestRepository.findActiveGuests().map { guestList =>
        composeAndSendEmailAllGuests(guestList, menuList)
      }
    }.onComplete {
      case Success(_) => // Log success or add additional actions if needed
      case Failure(exception) =>
        // Log the exception or handle it appropriately
        println(s"Error occurred while sending menu to guests: ${exception.getMessage}")
    }
  }
}
