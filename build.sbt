import Dependencies._
val kafka_streams_scala_version = "0.1.2"
resolvers += Resolver.bintrayRepo("ovotech", "maven")
val kafkaSerializationV = "0.1.23" // see the Maven badge above for the latest version
val ScalatraVersion = "2.6.2"
resolvers += Classpaths.typesafeReleases

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "kafkaci",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "KafkaCI",
    javaOptions ++= Seq(
      "-Xdebug",
      "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
    ),

    libraryDependencies ++= Seq(
      "com.ovoenergy" %% "kafka-serialization-core" % kafkaSerializationV,
      "com.ovoenergy" %% "kafka-serialization-circe" % kafkaSerializationV,
      "io.circe" %% "circe-core" % "0.9.1",
      "io.circe" %% "circe-generic"  % "0.9.1",
      "io.circe" %% "circe-parser"  % "0.9.1",
      "com.lightbend" %% "kafka-streams-scala" % kafka_streams_scala_version,


      "org.scalatra" %% "scalatra" % ScalatraVersion,
      "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
      "org.eclipse.jetty" % "jetty-webapp" % "9.4.8.v20171121" % "container",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
      "org.scalatra" %% "scalatra-forms" % ScalatraVersion
    )
  )

enablePlugins(SbtTwirl)
enablePlugins(ScalatraPlugin)