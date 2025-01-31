////ThisBuild / version := "0.1.0-SNAPSHOT"
////
////ThisBuild / scalaVersion := "3.3.4"
////
////lazy val root = (project in file("."))
////  .settings(
////    name := "MicroservicesAndKafka-2"
////  )
//
//ThisBuild / version := "0.1.0-SNAPSHOT"
//
//ThisBuild / scalaVersion := "2.13.13"
//
//lazy val root = (project in file("."))
//  .settings(
//    name := "kafka-consumer-person"
//  )
//
//resolvers += "Akka library repository".at("https://repo.akka.io/maven")
//
//lazy val akkaVersion = sys.props.getOrElse("akka.version", "2.9.3")
//
//// Run in a separate JVM, to make sure sbt waits until all threads have
//// finished before returning.
//// If you want to keep the application running while executing other
//// sbt tasks, consider https://github.com/spray/sbt-revolver/
//fork := true
//
//libraryDependencies ++= Seq(
//  "com.typesafe.akka" %% "akka-actor" % "2.9.3",
//  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
//  "ch.qos.logback" % "logback-classic" % "1.2.13",
//  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
//  "org.scalatest" %% "scalatest" % "3.2.15" % Test
//)
//
//libraryDependencies ++= Seq(
//  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
//  "com.typesafe.akka" %% "akka-stream-kafka" % "6.0.0",
//  "com.typesafe.akka" %% "akka-http" % "10.6.3",
//  "com.typesafe.akka" %% "akka-http-spray-json" % "10.6.3",
//  "io.spray" %% "spray-json" % "1.3.6",
//  "com.typesafe.akka" %% "akka-slf4j" % "2.9.3",
//  "com.typesafe.akka" %% "akka-protobuf-v3" % "2.9.3",
//  "org.apache.kafka" %% "kafka" % "3.7.0" // Kafka client
//)

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "kafka-consumer-person"
  )

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

lazy val akkaVersion = sys.props.getOrElse("akka.version", "2.6.20") // Compatible Akka version

fork := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.13",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "2.1.1",
  "com.typesafe.akka" %% "akka-http" % "10.2.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10",
  "io.spray" %% "spray-json" % "1.3.6",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-protobuf-v3" % akkaVersion,
  "org.apache.kafka" %% "kafka" % "3.7.0"
)
