name := "reactive-maps"

version := "1.0-SNAPSHOT"
val AkkaVersion = "2.6.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
  "de.grundid.opendatalab" % "geojson-jackson" % "1.1",
  "org.webjars" % "bootstrap" % "3.0.0",
  "org.webjars" % "knockout" % "2.3.0",
  "org.webjars" % "requirejs" % "2.1.11-1",
  "org.webjars" % "leaflet" % "0.7.2",
  "org.webjars" % "rjs" % "2.1.11-1-trireme" % "test",
  "org.webjars" % "squirejs" % "0.1.0" % "test"
)
libraryDependencies += guice

scalaVersion := "2.13.1"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

MochaKeys.requires += "SetupMocha.js"

pipelineStages := Seq(rjs, digest, gzip)

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node
