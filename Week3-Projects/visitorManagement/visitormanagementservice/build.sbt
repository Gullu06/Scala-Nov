import scala.collection.Seq

name := """visitormanagementservice"""

organization := "com.playapp"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.14"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.30"
libraryDependencies ++= Seq(
  "org.playframework" %% "play-slick"            % "6.1.0",
  "org.playframework" %% "play-slick-evolutions" % "6.1.0",
  "mysql" % "mysql-connector-java" % "8.0.26"

)
libraryDependencies += ws

libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka" % "3.0.0",
  "com.typesafe.play" %% "play-json" % "2.9.2"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.6.20", // Akka Streams
  "com.typesafe.akka" %% "akka-actor" % "2.6.20",  // Akka Actor
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.20",
  "org.apache.pekko" %% "pekko-stream" % "1.0.1",
  "com.auth0" % "java-jwt" % "4.3.0", // Java JWT library
  "com.typesafe.play" %% "play-json" % "2.9.4" // Play JSON for JSON processing
)

libraryDependencies += filters
