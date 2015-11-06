lazy val root = (project in file(".")).
  settings(
    name := "crawlpod",
    organization := "net.crawlpod",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.11.5"
  )
  
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "com.typesafe.akka" %% "akka-http-experimental" % "1.0"
)