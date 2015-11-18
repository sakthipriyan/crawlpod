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
  val controller = system.actorOf(Props(classOf[ControllerActor]), "controller")
  val extractor = system.actorOf(Props(classOf[ExtractActor]), "extractor")

  val crawler = system.actorOf(Props(classOf[HttpActor],
    Http(httpProvider)), "http")

  val queue = system.actorOf(Props(classOf[QueueActor],
    Queue(queueProvider)), "queue")

  val rawStore = system.actorOf(Props(classOf[RawStoreActor],
    RawStore(rawStoreProvider)), "rawstore")

  val jsonStore = system.actorOf(Props(classOf[JsonStoreActor],
    JsonStore(jsonStoreProvider)), "jsonstore")

  val requestStore = system.actorOf(Props(classOf[RequestStoreActor],
    RequestStore(requestStoreProvider)), "requeststore")

  controller ! Tick
}
