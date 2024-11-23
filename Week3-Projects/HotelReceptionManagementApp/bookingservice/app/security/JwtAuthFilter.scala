package security
import org.apache.pekko.stream.Materializer
import play.api.mvc._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
class JwtAuthFilter @Inject()(
                               implicit val mat: Materializer,
                               ec: ExecutionContext
                             ) extends Filter {
  println("JwtAuthFilter initialized!") // Debug log
  override def apply(nextFilter: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
    val publicRoutes = Seq("/api/login", "/persons")
    println(s"Processing request path: ${request.path}")
    if (publicRoutes.exists(request.path.startsWith)) {
      println("Public route detected. Skipping authentication.")
      nextFilter(request)
    } else {
      val tokenOpt = request.headers.get("Authorization").map(_.replace("Bearer ", ""))
      tokenOpt match {
        case Some(token) =>
          JwtUtil.validateToken(token) match {
            case Some(userId) =>
              println(s"Valid token for user: $userId")
              nextFilter(request)
            case None =>
              println("Invalid or expired token.")
              Future.successful(Results.Unauthorized("Invalid or missing token"))
          }
        case None =>
          println("Authorization header missing.")
          Future.successful(Results.Unauthorized("Authorization header missing or invalid."))
      }
    }
  }
}