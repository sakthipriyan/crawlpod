package net.crawlpod.core

import org.json4s.JsonAST.JObject
import net.crawlpod.driver._
import scala.concurrent.Future
import com.typesafe.config.ConfigFactory
import net.crawlpod.driver.DispatchHttp

/**
 * @author sakthipriyan
 */

object Queue {
  def apply(name: String): Queue = name match {
    case "MongodbQueue" => new MongodbQueue
    case _              => throw new RuntimeException(s"Invalid provider name for the queue $name")
  }
}

object RawStore {
  def apply(name: String): RawStore = name match {
    case "MongodbRawStore" => new MongodbRawStore
    case _                 => throw new RuntimeException(s"Invalid provider name for the raw store $name")
  }
}

object JsonStore {
  def apply(name: String): JsonStore = name match {
    case "MongodbJsonStore" => new MongodbJsonStore
    case _                  => throw new RuntimeException(s"Invalid provider name for the json store $name")
  }
}

object Http {
  def apply(name: String): Http = name match {
    case "DispatchHttp" => new DispatchHttp
    case _              => throw new RuntimeException(s"Invalid provider name for the Http $name")
  }
}

object RequestStore {
  def apply(name: String): RequestStore = name match {
    case "MongodbRequestStore" => new MongodbRequestStore
    case _              => throw new RuntimeException(s"Invalid provider name for the ExtractStore $name")
  }
}

trait Queue {
  def enqueue(r: List[CrawlRequest]): Future[Unit]
  def dequeue: Future[Option[CrawlRequest]]
  def doneSize: Future[Long]
  def queueSize: Future[Long]
  def failed(req: CrawlRequest): Future[Unit]
  def failedSize: Future[Long]
  def empty: Future[Unit]
  def shutdown: Unit
}

trait RawStore {
  def put(res: CrawlResponse): Future[Unit]
  def get(req: CrawlRequest): Future[Option[CrawlResponse]]
  def count: Future[Long]
  def empty: Future[Unit]
  def shutdown: Unit
}

trait RequestStore {
  def setProcessed(request: CrawlRequest, ts:Long = System.currentTimeMillis):Future[Unit]
  def isProcessed(request: CrawlRequest, afterTs: Long = 0): Future[Boolean]
  def count: Future[Long]
  def empty: Future[Unit]
  def shutdown: Unit
}

trait JsonStore {
  def write(json: List[JObject]): Future[Unit]
  def count: Future[Long]
  def empty: Future[Unit]
  def shutdown: Unit
}

trait Http {
  def crawl(request: CrawlRequest): Future[CrawlResponse]
}
