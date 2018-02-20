import Dependencies._
val kafka_streams_scala_version = "0.1.2"
resolvers += Resolver.bintrayRepo("ovotech", "maven")
val kafkaSerializationV = "0.1.23" // see the Maven badge above for the latest version


lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "KafkaCI",

    libraryDependencies ++= Seq(
      "com.ovoenergy" %% "kafka-serialization-core" % kafkaSerializationV,
      "com.ovoenergy" %% "kafka-serialization-circe" % kafkaSerializationV,
      "io.circe" %% "circe-core" % "0.9.1",
      "io.circe" %% "circe-generic"  % "0.9.1",
      "io.circe" %% "circe-parser"  % "0.9.1",
      "com.lightbend" %% "kafka-streams-scala" % kafka_streams_scala_version,
      "com.typesafe.akka" %% "akka-http"   % "10.1.0-RC2",
      "com.typesafe.akka" %% "akka-stream" % "2.5.9"
    )
)
