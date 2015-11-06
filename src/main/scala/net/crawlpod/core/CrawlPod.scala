package net.crawlpod.core

import akka.actor.ActorSystem
import scala.concurrent.Future
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * @author sakthipriyan
 */
object CrawlPod extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val responseFuture: Future[HttpResponse] =
    Http().singleRequest(HttpRequest(uri = "http://akka.io"))

  for (response <- responseFuture) {
    println(response.entity)
  }
  
  Thread.sleep(10000)

}