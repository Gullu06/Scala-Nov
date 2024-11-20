package controllers

import models.{Menu, MenuDAO}
import play.api.libs.json._
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.Logging

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MenuController @Inject()(menuDao: MenuDAO, cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with Logging {

  // Implicit format for serializing/deserializing Menu
  implicit val menuFormat: Format[Menu] = Json.format[Menu]

  /**
   * Inserts a batch of menu items into the database.
   */
  def insertMenu(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Seq[Menu]] match {
      case JsSuccess(itemsToInsert, _) =>
        menuDao.insertMenuItem(itemsToInsert).map {
          case Some(count) =>
            Ok(Json.obj("message" -> s"Inserted $count items"))
          case None =>
            Ok(Json.obj("message" -> "Menu items inserted successfully"))
        }.recover {
          case ex =>
            logger.error("Failed to insert menu items", ex)
            InternalServerError(Json.obj("message" -> s"Failed to insert menu items: ${ex.getMessage}"))
        }

      case JsError(errors) =>
        val errorDetails = errors.map { case (path, validationErrors) =>
          s"${path.toString()}: ${validationErrors.map(_.message).mkString(", ")}"
        }.mkString("; ")

        logger.warn(s"Invalid JSON provided: $errorDetails")
        Future.successful(BadRequest(Json.obj(
          "message" -> "Invalid JSON provided",
          "errors" -> errorDetails
        )))
    }
  }

  /**
   * Fetches all menu items from the database.
   */
  def fetchMenu(): Action[AnyContent] = Action.async {
    menuDao.list().map { menuItems =>
      Ok(Json.toJson(menuItems))
    }.recover {
      case ex =>
        logger.error("Failed to fetch menu items", ex)
        InternalServerError(Json.obj("message" -> s"Failed to fetch menu items: ${ex.getMessage}"))
    }
  }
}
