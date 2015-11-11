lazy val root = (project in file(".")).
  settings(
    name := "crawlpod",
    organization := "net.crawlpod",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.11.5"
  )
  
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.0",
  "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.reactivemongo" %% "reactivemongo" % "0.11.7",
  "org.jsoup" % "jsoup" % "1.8.3"
)