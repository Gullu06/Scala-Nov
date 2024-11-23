package controllers

import play.api.mvc._
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SecuredController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def securedEndpoint: Action[AnyContent] = Action { request =>
    Ok("This is a secured endpoint! You are authenticated.")
  }
}
