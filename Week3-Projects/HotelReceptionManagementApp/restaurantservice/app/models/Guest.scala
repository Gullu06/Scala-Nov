package models

import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Json, Reads}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class Guest(guest_id: Long, name: String, room_no: Int, email: String, address: String, guest_status: String)

object Guest {
  // Define the Reads for Person to allow Play JSON to map JSON to the case class
  implicit val guestReads: Reads[Guest] = Json.reads[Guest]
}

class GuestDao @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class GuestTable(tag: Tag) extends Table[Guest](tag, "guest") {
    def guest_id = column[Long]("guest_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def room_no = column[Int]("room_no")
    def email = column[String]("email")
    def address = column[String]("address")
    def guest_status = column[String]("guest_status")

    def * = (guest_id, name, room_no, email, address, guest_status) <> ((Guest.apply _).tupled, Guest.unapply)
  }

  val guest = TableQuery[GuestTable]

  def findActiveGuests(): Future[Seq[Guest]] = db.run{
    guest.filter(_.guest_status === "ACTIVE" ).result
  }
}