package net.crawlpod.core

import akka.actor.Actor
import akka.actor.ActorLogging

/**
 * @author sakthipriyan
 */

class ControllerActor extends Actor with ActorLogging {
  import scala.concurrent.duration._
  import scala.language.postfixOps
  import context._

  override def preStart() = system.scheduler.scheduleOnce(500 millis, self, Tick)

  // override postRestart so we don't call preStart and schedule a new message
  override def postRestart(reason: Throwable) = {}

  override def receive = {
    case Tick => {
      system.scheduler.scheduleOnce(1000 millis, self, Tick)
      log.info("received unknown message")
    }
    case Stop => log.info("received unknown message")
    case _    => log.warning("Received unknown message")

  }

}

class QueueActor extends Actor with ActorLogging {
  def receive = {
    case e: Enqueue => log.info("{} crawl requests enqueued", e.requests.size)
    case d: Dequeue => {
      log.info("Dequeued {} crawl requests", d.count)
    }
    case _ => log.info("received unknown message")
  }
}

class CrawlActor extends Actor with ActorLogging {
  def receive = {
    case _ => log.info("received unknown message")
  }
}

class ExtractActor extends Actor with ActorLogging {
  def receive = {
    case _ => log.info("received unknown message")
  }
}

class RawStoreActor extends Actor with ActorLogging {
  def receive = {
    case w: RawStoreWrite => log.info("received unknown message, {}", w)
    case r: RawStoreRead  => log.info("received unknown message, {}", r)
    case _                => log.warning("Received unknown message")
  }
}

class JsonStoreActor extends Actor with ActorLogging {
  def receive = {
    case JsonWrite => log.info("Json write received, {}")
    case _         => log.warning("Received unknown message {}")
  }
}
