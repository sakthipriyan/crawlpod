package net.crawlpod.core

import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.ExecutionContext.Implicits.global
import net.crawlpod.driver._
import com.typesafe.config.ConfigFactory

/**
 * @author sakthipriyan
 */
object CrawlPod extends App {

  val system = ActorSystem("crawlpod")
  val controller = system.actorOf(Props(classOf[ControllerActor]), "controller")
  val extractor = system.actorOf(Props(classOf[ExtractActor]), "extractor")

  val crawler = system.actorOf(Props(classOf[HttpActor],
    Http(Config.cfg.getString("crawlpod.provider.http"))), "http")

  val queue = system.actorOf(Props(classOf[QueueActor],
    Queue(Config.cfg.getString("crawlpod.provider.queue"))), "queue")

  val rawStore = system.actorOf(Props(classOf[RawStoreActor],
    RawStore(Config.cfg.getString("crawlpod.provider.rawstore"))), "rawstore")

  val jsonStore = system.actorOf(Props(classOf[JsonStoreActor],
    JsonStore(Config.cfg.getString("crawlpod.provider.jsonstore"))), "jsonstore")

  controller ! Tick
}
