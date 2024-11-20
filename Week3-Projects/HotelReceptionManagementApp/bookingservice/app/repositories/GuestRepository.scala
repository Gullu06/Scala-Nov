package repositories

import models.Guest
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

class GuestRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private class GuestTable(tag: Tag) extends Table[Guest](tag, "Guest") {
    def guest_id = column[Long]("guest_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def room_no = column[Int]("room_no")
    def email = column[String]("email")
    def address = column[String]("address")
    def id_proof = column[Array[Byte]]("id_proof")
    def guest_status = column[String]("guest_status")

    def * = (guest_id, name, room_no, email, address, id_proof, guest_status) <> ((Guest.apply _).tupled, Guest.unapply)
  }

  private val guests = TableQuery[GuestTable]

  // Method to add guests to the database
  def addGuestsAndReturnIds(guestList: Seq[Guest]): Future[Seq[Long]] = {
    val addGuestsAction = guestList.map(guest => (guests returning guests.map(_.guest_id)) += guest)
    db.run(DBIO.sequence(addGuestsAction).transactionally)
  }

  def findGuestsByRoomNo(room_no: Int): Future[Seq[Guest]] = db.run {
    guests.filter(_.room_no === room_no).result
  }
  def updateGuestStatus(guest_id: Long, status: String): Future[Int] = db.run {
    guests.filter(_.guest_id === guest_id).map(_.guest_status).update(status)
  }

  def updateGuestsStatusByRoomNo(room_no: Int, status: String): Future[Int] = db.run {
    guests.filter(_.room_no === room_no).map(_.guest_status).update(status)
  }

  def getActiveGuests: Future[Seq[Guest]] = db.run {
    guests.filter(_.guest_status === "ACTIVE").result
  }
}
