name := """bookingService"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.15"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test
//libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.8.6"
//libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.8.6"
//libraryDependencies += "com.lightbend.akka" %% "akka-http" % "10.2.6"


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
