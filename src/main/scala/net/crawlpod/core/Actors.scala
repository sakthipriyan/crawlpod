package net.crawlpod.core

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSelection.toScala
import akka.pattern.pipe
import akka.util.ByteString
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future

/**
 * @author sakthipriyan
 */
class ControllerActor extends Actor with ActorLogging {
  import scala.concurrent.duration._
  import context._
  import scala.language.postfixOps

  override def preStart() = system.scheduler.scheduleOnce(500 millis, self, Tick)

  override def postRestart(reason: Throwable) = {}

  override def receive = {
    case Tick => {
      system.scheduler.scheduleOnce(10000 millis, self, Tick)
      context.actorSelection("../queue") ! Dequeue
      log.debug("Received Tick")
    }
    case Stop => log.debug("Received Stop")
    case x    => log.warning("Received unknown message: {}", x)
  }
}

class HttpActor(http: Http) extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  def receive = {
    case request: CrawlRequest => {
      log.debug("Received {}", request)
      http.crawl(request) onComplete {
        case Success(response) => {
          context.actorSelection("../extractor") ! response
          context.actorSelection("../rawstore") ! response
        }
        case Failure(t) => {
          log.error("Failed to get response for {}", request, t)
          context.actorSelection("../queue") ! Failed(request)
        }
      }
    }
    case x => log.warning("Received unknown message: {}", x)
  }
}

class ExtractActor extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  def receive = {
    case response: CrawlResponse => {
      log.debug("Received {}", response)
      extract(response) onComplete {
        case Success(extract) => {
          context.actorSelection("../queue") ! Enqueue(extract.requests)
          context.actorSelection("../jsonstore") ! JsonWrite(extract.documents)
        }
        case Failure(t) => {
          log.error("Failed to dequeue CrawlRequest", t)
          context.actorSelection("../queue") ! Failed(response.request)
        }
      }
    }
    case x => log.warning("Received unknown message: {}", x)
  }

  def extract(response: CrawlResponse) = {
    Future {
      val ext = response.request.extractor
      val lastIndex = ext.lastIndexOf(".")
      val className = ext.substring(0, lastIndex)
      val methodName = ext.substring(lastIndex + 1, ext.length)
      val clazz = Class.forName(className)
      val obj = clazz.newInstance
      val method = clazz.getDeclaredMethod(methodName, response.getClass)
      method.invoke(obj, response).asInstanceOf[Extract]
    }
  }
}

class QueueActor(queue: Queue) extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  val cacheEnabled = Config.cfg.getBoolean("app.cache.enabled")
  def receive = {
    case e: Enqueue => {
      log.debug("Received {}", e)
      queue.enqueue(e.requests) onFailure {
        case t => log.error("Failed to enqueue {}", e.requests, t)
      }
    }
    case Dequeue => {
      log.debug("Received Dequeue")
      queue.dequeue onComplete {
        case Success(reqOpt) => {
          for (request <- reqOpt) {
            val actor = if (cacheEnabled && request.cache) "../rawstore" else "../http"
            context.actorSelection(actor) ! request
          }
        }
        case Failure(t) => log.error("Failed to dequeue CrawlRequest", t)
      }
    }

    case f: Failed => {
      log.debug("Received {}", f)
      queue.failed(f.request) onFailure {
        case t => log.error("Failed to enqueue {}", f.request, t)
      }
    }

    case x => log.warning("Received unknown message: {}", x)
  }
}

class RawStoreActor(rawStore: RawStore) extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  val afterTs = Config.cfg.getLong("app.cache.ts")
  def receive = {
    case response: CrawlResponse => {
      log.debug("Received {}", response)
      rawStore.put(response).onFailure {
        case t => log.error("Failed to store {}", response, t)
      }
    }
    case request: CrawlRequest => {
      log.debug("Received {}", request)
      rawStore.get(request, afterTs) onComplete {
        case Success(response) => response match {
          case Some(response) => context.actorSelection("../extractor") ! response
          case None           => context.actorSelection("../http") ! request
        }
        case Failure(t) => log.error("Failed to retrieve response for {}", request, t)
      }
    }
    case x => log.warning("Received unknown message: {}", x)
  }
}

class JsonStoreActor(jsonStore: JsonStore) extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  def receive = {
    case w: JsonWrite => {
      log.debug("Received {}", w)
      jsonStore.write(w.list).onFailure {
        case t => log.error("Failed to store json {}", w.list, t)
      }
    }
    case x => log.warning("Received unknown message: {}", x)
  }
}
