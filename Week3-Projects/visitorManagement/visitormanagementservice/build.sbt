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
