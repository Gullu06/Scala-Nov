ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.13"

lazy val root = (project in file("."))
  .settings(
    name := "Day18AndDay19"
  )

val sparkVersion = "3.5.1"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-yarn" % sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.1",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.2",
  "com.google.cloud.bigdataoss" % "gcs-connector" % "hadoop3-2.2.5",
  "mysql" % "mysql-connector-java" % "8.0.19"
)