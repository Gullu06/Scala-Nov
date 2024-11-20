package repositories

import models.Room
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

class RoomRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private class RoomTable(tag: Tag) extends Table[Room](tag, "Room") {
    def room_id = column[Int]("room_id", O.PrimaryKey)
    def room_no = column[Int]("room_no")
    def floor_no = column[Int]("floor_no")
    def room_type = column[String]("room_type")
    def room_status = column[String]("room_status")
    def price = column[Double]("price")

    def * = (room_id, room_no, floor_no, room_type, room_status, price) <> ((Room.apply _).tupled, Room.unapply)
  }

  private val rooms = TableQuery[RoomTable]

  // Method to get all available rooms by type
  def getAvailableRoomsByType(room_type: String): Future[Seq[Room]] = db.run {
    rooms
      .filter(room => room.room_status === "AVAILABLE" && room.room_type === room_type)
      .result
  }

  // Method to update room status by RoomID
  def updateRoomStatusById(room_id: Int, status: String): Future[Int] = db.run {
    rooms.filter(_.room_id === room_id).map(_.room_status).update(status)
  }

  // Method to update room status by RoomNo
  def updateRoomStatusByRoomNo(room_no: Int, status: String): Future[Int] = db.run {
    rooms.filter(_.room_no === room_no).map(_.room_status).update(status)
  }

  def getRoomIdByRoomNo(room_no: Int): Future[Option[Int]] = db.run {
    rooms.filter(_.room_no === room_no).filter(_.room_status === "AVAILABLE").map(_.room_id).result.headOption
  }

  // Method to update the room status to OCCUPIED
  def findRoomStatusByRoomNo(room_no: Int, status: String = "OCCUPIED"): Future[Int] = db.run {
    rooms.filter(_.room_no === room_no).map(_.room_status).update(status)
  }

  def getRoomNoById(room_id: Int): Future[Option[Int]] = db.run {
    rooms.filter(_.room_id === room_id).map(_.room_no).result.headOption
  }
}
