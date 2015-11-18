package net.crawlpod.core

import akka.actor.ActorLogging
import akka.actor.Cancellable
import akka.actor.Actor

/**
 * @author sakthipriyan
 */
class ControllerActor extends Actor with ActorLogging {
  import scala.concurrent.duration._
  import context._
  import scala.language.postfixOps

  override def preStart() = {
    scheduler = system.scheduler.scheduleOnce(1 minute, self, Tick)
  }

  override def postRestart(reason: Throwable) = {}

  override def postStop(): Unit = scheduler.cancel()

  private var scheduler: Cancellable = _
  override def receive = {
    case Tick => {
      log.debug("Received Tick")
      scheduler.cancel()
      scheduler = system.scheduler.scheduleOnce(1 minute, self, Tick)
      context.actorSelection("../queue") ! Dequeue

    }
    case Stop => log.debug("Received Stop")
  }
}
