import akka.actor.{Actor, ActorRef}

class MessageHandler(networkProcessor: ActorRef, cloudProcessor: ActorRef, appProcessor: ActorRef) extends Actor {
  override def receive: Receive = {
    case msg: Message =>
      println(s"MessageHandler received message: ${msg.message} of type ${msg.messageType}")
      msg.messageType match {
        case "NetworkMessage" =>
          println("Forwarding to NetworkMessageProcessor")
          networkProcessor ! msg
        case "CloudMessage" =>
          println("Forwarding to CloudMessageProcessor")
          cloudProcessor ! msg
        case "AppMessage" =>
          println("Forwarding to AppMessageProcessor")
          appProcessor ! msg
        case _ =>
          println("Unknown message type.")
      }
  }
}
