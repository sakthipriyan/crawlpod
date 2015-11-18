package net.crawlpod.core

import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.ExecutionContext.Implicits.global
import net.crawlpod.driver._
import com.typesafe.config.ConfigFactory
import net.crawlpod.util.ConfigUtil._

/**
 * @author sakthipriyan
 */
object CrawlPod extends App {

  val system = ActorSystem("crawlpod")
  
  val queue = Queue(queueProvider)
  val requestStore = RequestStore(requestStoreProvider)
  val rawStore = RawStore(rawStoreProvider)
  val http = Http(httpProvider)
  val jsonStore = JsonStore(jsonStoreProvider)
  
  val controllerActor = system.actorOf(Props(classOf[ControllerActor]), "controller")

  val queueActor = system.actorOf(Props(classOf[QueueActor], queue), "queue")
  val requestStoreActor = system.actorOf(Props(classOf[RequestStoreActor], requestStore), "requeststore")
  val rawStoreActor = system.actorOf(Props(classOf[RawStoreActor], rawStore), "rawstore")
  val httpActor = system.actorOf(Props(classOf[HttpActor], http), "http")
  val extractorActor = system.actorOf(Props(classOf[ExtractActor]), "extractor")  
  val jsonStoreActor = system.actorOf(Props(classOf[JsonStoreActor], jsonStore), "jsonstore")
  val statsActor = system.actorOf(Props(classOf[StatsActor], queue, requestStore, rawStore, http, jsonStore), "stats")
}
