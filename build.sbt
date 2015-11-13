lazy val root = (project in file(".")).
  settings(
    name := "crawlpod",
    organization := "net.crawlpod",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.11.5"
  )

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.13",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "ch.qos.logback" % "logback-core" % "1.1.3",
  "com.typesafe.akka" %% "akka-actor" % "2.4.0",
  "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.jsoup" % "jsoup" % "1.8.3",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.0.0"
  
)

net.virtualvoid.sbt.graph.Plugin.graphSettings