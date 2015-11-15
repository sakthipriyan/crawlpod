package net.crawlpod.driver

import scala.concurrent.{ Future, Promise }
import scala.util.{ Success, Failure }
import org.json4s.JsonAST.JObject
import org.mongodb.scala._
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory
import net.crawlpod.core._
import org.mongodb.scala.bson.conversions.Bson
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._
import org.mongodb.scala.Observer

/**
 * @author sakthipriyan
 */

class MongodbQueue extends Queue {

  val queue = Mongodb.collection("mongodb.collection.queue")
  val failed = Mongodb.collection("mongodb.collection.failed")

  val usedFalse = Document("used" -> false)
  val usedTrue = Document("used" -> true)
  val setUsedTrue = Document("$set" -> usedTrue)

  override def enqueue(requests: List[CrawlRequest]): Future[Unit] = {
    val req = requests.map { r => Document(r.toJsonString) ++ usedFalse }.toSeq
    queue.insertMany(req).head.map(a => Unit)
  }

  override def dequeue: Future[Option[CrawlRequest]] = {
    val dequeue = Promise[Option[CrawlRequest]]
    queue.findOneAndUpdate(usedFalse, setUsedTrue).subscribe(new Observer[Document]() {
      override def onNext(doc: Document): Unit = dequeue.success(Some(parseCrawlRequest(doc)))
      override def onError(e: Throwable): Unit = dequeue.failure(e)
      override def onComplete(): Unit = if (!dequeue.isCompleted) dequeue.success(None)
    })
    dequeue.future
  }

  override def queueSize: Future[Long] = queue.count.head

  override def doneSize: Future[Long] = queue.count(usedTrue).head

  override def failed(r: CrawlRequest) = {
    val request = Document(r.toJsonString)
    failed.insertOne(request).head.map(a => Unit)
  }
  override def failedSize: Future[Long] = failed.count.head

  override def empty = {
    val result = Promise[Unit]
    for {
      q <- queue.deleteMany(Document()).head
      f <- failed.deleteMany(Document()).head
    } {
      result.success(Unit)
    }
    result.future
  }

  override def shutdown = Mongodb.shutdown

  private def parseCrawlRequest(doc: Document) = {
    val url = getString(doc, "url")
    val extractor = getString(doc, "extractor")
    val method = getString(doc, "method")
    val headers = getOptMap(doc, "headers")
    val passData = getOptMap(doc, "passData")
    val requestBody = doc.get("requestBody") match {
      case None    => None
      case Some(b) => Some(b.asString.getValue)
    }
    val cache = doc.get("cache").get.asBoolean.getValue
    CrawlRequest(url, extractor, method, headers, passData, requestBody, cache)
  }

  private def getString(doc: Document, key: String) = doc.get(key).get.asString.getValue

  private def getOptMap(doc: Document, key: String) = doc.get(key) match {
    case Some(map) => Some(map.asDocument().mapValues(v => v.asString.getValue).toMap)
    case None      => None
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
