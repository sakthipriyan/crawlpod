package net.crawlpod.core

import akka.actor.ActorLogging
import akka.actor.Actor

/**
 * @author sakthipriyan
 */
class StatsActor(queue: Queue, requestStore: RequestStore, rawStore: RawStore, jsonStore: JsonStore) extends Actor with ActorLogging {

  import scala.concurrent.ExecutionContext.Implicits.global
  def receive = {
    case Stats => {
      log.debug("Received Stats")
      queue.queueSize
      queue.doneSize
      queue.failedSize
      requestStore.count
      rawStore.count
      jsonStore.count
    }
    case x => log.warning("Received unknown message: {}", x)
  }
}
