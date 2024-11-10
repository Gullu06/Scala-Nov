ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "Task-2"
  )

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

lazy val akkaVersion = "2.6.20" // Stable Akka version compatible with Scala 2.13

fork := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.2.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10",
  "ch.qos.logback" % "logback-classic" % "1.2.13", // Logging
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "org.apache.kafka" %% "kafka" % "3.3.1", // Kafka client, matching Akka Kafka Streams compatibility
  "io.spray" %% "spray-json" % "1.3.6", // JSON support
  "com.typesafe.akka" %% "akka-stream-kafka" % "2.1.1", // Akka Kafka Streams compatible with Akka 2.6.x
  "org.scalatest" %% "scalatest" % "3.2.15" % Test
)
