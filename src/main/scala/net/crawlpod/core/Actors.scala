package net.crawlpod.core

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSelection.toScala
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri.apply
import akka.pattern.pipe
import akka.stream.scaladsl.ImplicitMaterializer
import akka.util.ByteString
import akka.http.scaladsl.model.HttpResponse

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
      context.actorSelection("../queue") ! new Dequeue
      log.info("received tick")
    }
    case Stop => log.info("received unknown message")
    case _    => log.warning("Received unknown message")

  }

}

class CrawlActor extends Actor with ActorLogging with ImplicitMaterializer {
  import akka.pattern.pipe
  import context.dispatcher
  import CrawlActor._
  val http = Http(context.system)
  def receive = {
    case r: CrawlRequest => {
      log.info("Received Crawl Request: " + r)
      for (response <- http.singleRequest(toHttpRequest(r))) {
        log.info("Response {}",response)
        context.actorSelection("../extractor") ! toCrawlResponse(r, response)
      }
    }
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      log.info("Got response, body: " + entity.dataBytes.runFold(ByteString(""))(_ ++ _))
    case HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
    case _ => log.info("received unknown message")
  }
}

object CrawlActor {
  def toHttpRequest(request: CrawlRequest) = {
    //method: HttpMethod = HttpMethods.GET, headers: Seq[HttpHeader] = immutable.this.Nil, entity: RequestEntity = HttpEntity.Empty, )
    HttpRequest(uri = request.url)
  }
  def toCrawlResponse(request: CrawlRequest, response: HttpResponse) = {
    CrawlResponse(200, request, null, null)
  }
}

class ExtractActor extends Actor with ActorLogging {
  def receive = {
    case response: CrawlResponse => {
      log.info("received message {}", response)
    }
    case _ => log.info("received unknown message")
  }
}

class QueueActor(queue: Queue) extends Actor with ActorLogging {
  def receive = {
    case e: Enqueue => {
      queue.enqueue(e.requests)
      log.info("{} crawl requests enqueued", e.requests.size)
    }
    case d: Dequeue => {
      for (request <- queue.dequeue) {
        val actor = if (request.cache) "../rawstore" else "../crawl"
        context.actorSelection(actor) ! request
      }

      log.info("Dequeued {} crawl requests", d.count)
    }
    case _ => log.warning("received unknown message")
  }
}

class RawStoreActor(rawStore: RawStore) extends Actor with ActorLogging {
  def receive = {
    case response: CrawlResponse => {
      rawStore.put(response)
      log.info("received RawStoreWrite, {}", response)
    }
    case request: CrawlRequest => {
      rawStore.get(request) match {
        case Some(response) => context.actorSelection("../extractor") ! response
        case None           => context.actorSelection("../crawler") ! request
      }
      log.info("Received CrawlRequest, {}", request)
    }
    case _ => log.warning("Received unknown message")
  }
}

class JsonStoreActor(jsonStore: JsonStore) extends Actor with ActorLogging {
  def receive = {
    case w: JsonWrite => {
      jsonStore.write(w.list)
      log.info("Json write received, {}", w)
    }
    case _ => log.warning("Received unknown message {}")
  }
}
