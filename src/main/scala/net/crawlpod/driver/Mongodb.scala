package net.crawlpod.driver

import scala.concurrent.Future
import scala.concurrent.Promise

import org.json4s.JsonAST.JObject
import org.mongodb.scala.Completed
import org.mongodb.scala.Document
import org.mongodb.scala.MongoClient
import org.mongodb.scala.Observer
import org.slf4j.LoggerFactory

import com.typesafe.config.ConfigFactory

import net.crawlpod.core.CrawlRequest
import net.crawlpod.core.CrawlResponse
import net.crawlpod.core.JsonStore
import net.crawlpod.core.Queue
import net.crawlpod.core.RawStore

/**
 * @author sakthipriyan
 */

class MongodbQueue extends Queue {

  val log = LoggerFactory.getLogger(getClass)
  val queue = Mongodb.collection("mongodb.collection.queue")

  override def enqueue(requests: List[CrawlRequest]): Future[Unit] = {
    val unit = Promise[Unit]
    val req = requests.map { r => Document(r.toJsonString) + ("crawled" -> false, "queued" -> false) }.toSeq
    val completed = queue.insertMany(req).subscribe(new Observer[Completed] {
      override def onNext(result: Completed): Unit = unit.success(Unit)
      override def onError(e: Throwable): Unit = unit.failure(e)
      override def onComplete(): Unit = {}
    })
    unit.future
  }

  override def dequeue: Option[CrawlRequest] = {
    Some(CrawlRequest("http://google.com", "net.crawlpod.extract.Google"))
  }
  override def failed(req: CrawlRequest, res: CrawlResponse) = {}

  override def size: Future[Long] = {
    val count = Promise[Long]
    queue.count().subscribe(new Observer[Long] {
      override def onNext(result: Long): Unit = {count.success(result)}
      override def onError(e: Throwable): Unit = log.error("Error while inserting into queue", e)
      override def onComplete(): Unit = log.debug("onComplete")
    })
    count.future
  }

  override def completed: Future[Long] = {
    val count = Promise[Long]
    queue.count().subscribe(new Observer[Long] {
      override def onNext(result: Long): Unit = { count.success(result) }
      override def onError(e: Throwable): Unit = log.error("Error while inserting into queue", e)
      override def onComplete(): Unit = log.debug("onComplete")
    })
    count.future
  }

  override def empty = {}
  override def shutdown = {
    Mongodb.shutdown
  }
}

class MongodbRawStore extends RawStore {
  //val queue = Mongodb.collection("mongodb.collection.raw")
  override def put(res: CrawlResponse) = {}
  override def get(req: CrawlRequest): Option[CrawlResponse] = None
  override def count: Long = 0
  override def empty: Unit = {} //queue.remove(null, null, false)
  override def shutdown = Mongodb.shutdown
}

class MongodbJsonStore extends JsonStore {
  //val queue = Mongodb.collection("mongodb.collection.json")
  override def write(json: List[JObject]) = {}
  override def count: Long = 0
  override def empty = {}
  override def shutdown = Mongodb.shutdown
}

object Mongodb {
  val config = ConfigFactory.load()
  val client = MongoClient(config.getString("mongodb.url"))
  val database = client.getDatabase(config.getString("mongodb.database"))
  var shutdownInitiated = false
  def collection(name: String) = database.getCollection(config.getString(name))
  def shutdown = {
    this.synchronized {
      if (!shutdownInitiated) {
        shutdownInitiated = true
        Thread.sleep(1000)
        client.close
      }
    }

  }
}
