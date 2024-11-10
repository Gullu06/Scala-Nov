import akka.actor.{ActorSystem, Props}

object Microservice extends App {
  implicit val system: ActorSystem = ActorSystem("ListenerSystem")

  // Create Kafka producer
  val producer = KafkaProducerFactory.createProducer()

  // Create the MessageGatherer actor
  val messageGatherer = system.actorOf(Props(new MessageGatherer(producer)), "messageGatherer")

  // Create listener actors, each passing the MessageGatherer reference
  val cloudListener = system.actorOf(Props(new CloudListener(messageGatherer)), "cloudListener")
  val networkListener = system.actorOf(Props(new NetworkListener(messageGatherer)), "networkListener")
  val appListener = system.actorOf(Props(new AppListener(messageGatherer)), "appListener")

  println("Microservice is running with listeners and MessageGatherer.")
}
