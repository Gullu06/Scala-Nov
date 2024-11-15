package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import repositories.VisitorRepository
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckoutController @Inject()(cc: ControllerComponents, visitorRepository: VisitorRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def checkoutVisitor() = Action.async(parse.json) { implicit request =>
    val jsonBody = request.body
    val visitorEmail = (jsonBody \ "visitorEmail").as[String]

    // Update the visitor's status to "CHECKED_OUT" based on their email
    visitorRepository.updateVisitorStatus(visitorEmail, "CHECKED_OUT").map { updated =>
      if (updated) {
        Ok(Json.obj("message" -> s"Visitor with email $visitorEmail successfully checked out."))
      } else {
        NotFound(Json.obj("error" -> "Visitor not found or failed to update status."))
      }
    }
  }
}
