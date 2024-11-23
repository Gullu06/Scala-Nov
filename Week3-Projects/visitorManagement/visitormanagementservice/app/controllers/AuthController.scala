package controllers

import play.api.mvc._
import play.api.libs.json._
import security.JwtUtil

import javax.inject.Inject

class AuthController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def login: Action[JsValue] = Action(parse.json) { request =>
    val username = (request.body \ "username").as[String]
    val password = (request.body \ "password").as[String]

    // Replace with your authentication logic
    if (username == "admin" && password == "password") {
      val token = JwtUtil.generateToken(username)
      Ok(Json.obj("token" -> token))
    } else {
      Unauthorized("Invalid credentials")
    }
  }
}