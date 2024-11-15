package repositories

import models.Visitor
import java.sql.Timestamp
import javax.inject._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{Future, ExecutionContext}

@Singleton
class VisitorRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class VisitorTable(tag: Tag) extends Table[Visitor](tag, "visitors") {
    def visitorId = column[Long]("visitor_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def contactNo = column[String]("contact_no")
    def address = column[String]("address")
    def hostName = column[String]("host_name")
    def hostEmail = column[String]("host_email")
    def visitorEmail = column[String]("visitor_email") // New column for visitor's email
    def purpose = column[String]("purpose")
    def idProof = column[Array[Byte]]("id_proof")
    def block = column[String]("block")
    def status = column[String]("status")
    def checkinTime = column[Timestamp]("checkin_time")
    def checkoutTime = column[Option[Timestamp]]("checkout_time")

    // Mapping the fields to the Visitor case class
    def * = (visitorId.?, name, contactNo, address, hostName, hostEmail, visitorEmail, purpose, idProof, block, status, checkinTime, checkoutTime) <> ((Visitor.apply _).tupled, Visitor.unapply)
  }

  private val visitors = TableQuery[VisitorTable]

  def addVisitor(visitor: Visitor): Future[Long] = db.run {
    (visitors returning visitors.map(_.visitorId)) += visitor
  }

  def updateVisitorStatus(email: String, newStatus: String): Future[Boolean] = db.run {
    visitors
      .filter(_.visitorEmail === email) // Find visitor by email
      .map(_.status) // Select only the status column
      .update(newStatus) // Update the status column to newStatus
      .map(_ > 0) // Return true if at least one row was updated
  }
}


