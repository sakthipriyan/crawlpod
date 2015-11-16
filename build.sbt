lazy val root = (project in file(".")).
  settings(
    name := "crawlpod",
    organization := "net.crawlpod",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.11.5"
  )

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.akka" %% "akka-actor" % "2.4.0",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.jsoup" % "jsoup" % "1.8.3",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.0.0",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

net.virtualvoid.sbt.graph.Plugin.graphSettings