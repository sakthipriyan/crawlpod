package net.crawlpod.core

import akka.actor.Actor
import akka.actor.ActorLogging

/**
 * @author sakthipriyan
 */
class QueueActor extends Actor with ActorLogging {
  def receive = {
    case e: Enqueue => log.info("{} crawl requests enqueued", e.requests.size)
    case d: Dequeue => log.info("Dequeued {} crawl requests", d.count)
    case _          => log.info("received unknown message")
  }
}
