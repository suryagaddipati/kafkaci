import Dependencies._
val kafka_streams_scala_version = "0.1.2"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Hello",

    libraryDependencies ++= Seq(
      "com.lightbend" %% "kafka-streams-scala" % kafka_streams_scala_version,
      "com.typesafe.akka" %% "akka-http"   % "10.1.0-RC2",
      "com.typesafe.akka" %% "akka-stream" % "2.5.9"
    )
  )
