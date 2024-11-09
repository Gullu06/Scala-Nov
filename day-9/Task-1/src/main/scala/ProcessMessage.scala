import spray.json._
import spray.json.DefaultJsonProtocol._

case class ProcessMessage(message: String, messageKey: String)

object JsonFormats {
  import spray.json._
  implicit val processMessageFormat: RootJsonFormat[ProcessMessage] = jsonFormat2(ProcessMessage)
}
