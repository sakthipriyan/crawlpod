package net.crawlpod.core

import akka.actor.ActorSystem
import akka.actor.Props

/**
 * @author sakthipriyan
 */
object CrawlPod extends App {
  val system = ActorSystem("crawlpod")
  val queue = system.actorOf(Props[QueueActor], "queue")
  
  queue  ! "hello"
  system.shutdown()
}
