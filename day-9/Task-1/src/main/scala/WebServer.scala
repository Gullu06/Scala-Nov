import akka.actor.{ActorSystem, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import spray.json._
import scala.concurrent.ExecutionContextExecutor

trait MessageJsonProtocol extends DefaultJsonProtocol {
  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat3(Message)
}

object WebServer extends App with MessageJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("MessagingActorSystem")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Create Kafka producer
  val producer = KafkaProducerFactory.createProducer()

  // Create actors
  val networkProcessor: ActorRef = system.actorOf(Props(new NetworkMessageProcessor(producer)))
  val cloudProcessor: ActorRef = system.actorOf(Props(new CloudMessageProcessor(producer)))
  val appProcessor: ActorRef = system.actorOf(Props(new AppMessageProcessor(producer)))
  val messageHandler: ActorRef = system.actorOf(Props(new MessageHandler(networkProcessor, cloudProcessor, appProcessor)))

  // Akka HTTP route
  val route =
    path("process-message") {
      post {
        entity(as[Message]) { msg =>
          println(s"WebServer received message: ${msg.message} of type ${msg.messageType}") // Debug log
          messageHandler ! msg
          complete(StatusCodes.OK, s"Message processed: ${msg.message}")
        }
      }
    }


  // Start the HTTP server
  Http().newServerAt("localhost", 8080).bind(route)
  println("Server online at http://localhost:8080/")
}
