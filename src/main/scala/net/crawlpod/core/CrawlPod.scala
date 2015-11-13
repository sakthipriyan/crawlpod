package net.crawlpod.core

import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.ExecutionContext.Implicits.global
import net.crawlpod.external.Mongodb
import net.crawlpod.external.MongodbQueue
import net.crawlpod.external.MongodbJsonStore
import net.crawlpod.external.MongodbRawStore
import com.typesafe.config.ConfigFactory

/**
 * @author sakthipriyan
 */
object CrawlPod extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem("crawlpod")
  val controller = system.actorOf(Props(classOf[ControllerActor]), "controller")
  val crawler = system.actorOf(Props(classOf[CrawlActor]), "crawler")
  val extractor = system.actorOf(Props(classOf[ExtractActor]), "extractor")
  val queue = system.actorOf(Props(classOf[QueueActor],
    Queue(config.getString("crawlpod.provider.queue"))), "queue")
    
  val rawStore = system.actorOf(Props(classOf[RawStoreActor],
    RawStore(config.getString("crawlpod.provider.rawstore"))), "rawstore")
    
  val jsonStore = system.actorOf(Props(classOf[JsonStoreActor],
    JsonStore(config.getString("crawlpod.provider.jsonstore"))), "jsonstore")
    
  controller ! Tick
}
