package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import services.NotificationService
import repositories.VisitorRepository
import models.Visitor
import java.sql.Timestamp
import scala.concurrent.{ExecutionContext, Future}
import java.util.Base64

@Singleton
class CheckInController @Inject()(
                                   cc: ControllerComponents,
                                   notificationService: NotificationService,
                                   visitorRepository: VisitorRepository
                                 )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def checkInVisitor() = Action.async(parse.json) { implicit request =>
    val jsonBody = request.body

    // Extract visitor details from the request JSON
    val name = (jsonBody \ "name").as[String]
    val contactNo = (jsonBody \ "contact_no").as[String]
    val address = (jsonBody \ "address").as[String]
    val hostName = (jsonBody \ "host_name").as[String]
    val hostEmail = (jsonBody \ "host_email").as[String]
    val visitorEmail = (jsonBody \ "visitor_email").as[String] // Extract visitor email
    val purpose = (jsonBody \ "purpose").as[String]
    val block = (jsonBody \ "block").as[String]
    val idProofBase64 = (jsonBody \ "id_proof").as[String]

    // Decode base64 image
    val idProof = Base64.getDecoder.decode(idProofBase64)

    // Create Visitor instance
    val visitor = Visitor(
      name = name,
      contactNo = contactNo,
      address = address,
      hostName = hostName,
      hostEmail = hostEmail,
      visitorEmail = visitorEmail,
      purpose = purpose,
      idProof = idProof,
      block = block,
      status = "CHECKED_IN",
      checkinTime = new Timestamp(System.currentTimeMillis())
    )

    // Save visitor to the database
    visitorRepository.addVisitor(visitor).flatMap { visitorId =>
      // Notify relevant parties
      notificationService.notifyHostEmployee(name, hostEmail)
      notificationService.notifyITSupport(name, visitorEmail)
      notificationService.notifySecurity(name)

      Future.successful(Created(Json.obj("message" -> "Check-in successful", "visitorId" -> visitorId)))
    }
  }
}
