package net.crawlpod.core

import akka.actor.ActorSystem
import akka.actor.Props
import  scala.concurrent.ExecutionContext.Implicits.global
import net.crawlpod.core.client.MongodbQueue
import net.crawlpod.core.client.MongodbJsonStore
import net.crawlpod.core.client.MongodbRawStore


/**
 * @author sakthipriyan
 */
object CrawlPod extends App {
  val system = ActorSystem("crawlpod")
  val controller = system.actorOf(Props(classOf[ControllerActor]), "controller")
  val crawler = system.actorOf(Props(classOf[CrawlActor]), "crawler")
  val extractor = system.actorOf(Props(classOf[ExtractActor]), "extractor")
  val queue = system.actorOf(Props(classOf[QueueActor],new MongodbQueue), "queue")
  val rawStore = system.actorOf(Props(classOf[RawStoreActor], new MongodbRawStore), "rawstore")
  val jsonStore = system.actorOf(Props(classOf[JsonStoreActor], new MongodbJsonStore), "jsonstore")
  controller  ! Tick  
}
