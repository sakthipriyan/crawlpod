package net.crawlpod.core

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSelection.toScala
import akka.pattern.pipe
import akka.util.ByteString
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import akka.actor.Cancellable
import net.crawlpod.util.ConfigUtil._

/**
 * @author sakthipriyan
 */

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
          context.actorSelection("../controller") ! Tick
          if (!extract.requests.isEmpty)
            context.actorSelection("../queue") ! Enqueue(extract.requests)
          if (!extract.documents.isEmpty)
            context.actorSelection("../jsonstore") ! JsonWrite(extract.documents)
        }
        case Failure(t) => {
          log.error("Failed to extract CrawlRequest", t)
          context.actorSelection("../controller") ! Tick
          context.actorSelection("../queue") ! Failed(response.request)
        }
      }
      context.actorSelection("../requeststore") ! MarkProcessed(response.request)
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
            context.actorSelection("../requeststore") ! Process(request)
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

  def receive = {
    case response: CrawlResponse => {
      log.debug("Received {}", response)
      rawStore.put(response).onFailure {
        case t => log.error("Failed to store {}", response, t)
      }
    }
    case request: CrawlRequest => {
      log.debug("Received {}", request)
      rawStore.get(request) onComplete {
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

class RequestStoreActor(requestStore: RequestStore) extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  def receive = {
    case MarkProcessed(request) => {
      log.debug("Received MarkProcessed for {}", request)
      requestStore.setProcessed(request) onFailure {
        case t => log.error("Failed to MarkProcessed {}", request, t)
      }
    }
    case Process(request) => {
      log.debug("Received IsProcessed for {}", request)
      requestStore.isProcessed(request, afterTs) onComplete {
        case Success(true) => log.debug("Skipping already processed {}", request)
        case Success(false) => {
          val actor = if (isCacheEnabled && request.cache) "../rawstore" else "../http"
          context.actorSelection(actor) ! request
        }
        case Failure(t) => log.error("Failed to retrieve response for {}", request, t)
      }
    }
    case x => log.warning("Received unknown message: {}", x)
  }
}
