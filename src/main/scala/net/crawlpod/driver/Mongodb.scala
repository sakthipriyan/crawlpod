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
import org.json4s.native.JsonMethods._
import java.net.URI

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
      override def onNext(doc: Document): Unit = dequeue.success(Some(Mongodb.parseCrawlRequest(doc)))
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
      q <- Mongodb.empty(queue)
      f <- Mongodb.empty(failed)
    } {
      result.success(Unit)
    }
    result.future
  }

  override def shutdown = Mongodb.shutdown
}

class MongodbRawStore extends RawStore {
  val raw = Mongodb.collection("mongodb.collection.rawstore")

  override def put(res: CrawlResponse): Future[Unit] = {
    raw.insertOne(Document(res.toJsonString) +
      ("_id" -> getId(res.request.url))).head.map(a => Unit)
  }

  override def get(request: CrawlRequest, afterTs: Long = 0): Future[Option[CrawlResponse]] = {
    val dequeue = Promise[Option[CrawlResponse]]
    raw.find(Document(
      "_id" -> getId(request.url),
      "created" -> Document("$gt" -> afterTs)))
      .subscribe(new Observer[Document]() {
        override def onNext(doc: Document): Unit = dequeue.success(Some(Mongodb.parseCrawlResponse(doc)))
        override def onError(e: Throwable): Unit = dequeue.failure(e)
        override def onComplete(): Unit = if (!dequeue.isCompleted) dequeue.success(None)
      })
    dequeue.future
  }
  override def count: Future[Long] = raw.count.head
  override def empty: Future[Unit] = Mongodb.empty(raw)
  override def shutdown = Mongodb.shutdown

  private def getId(url: String) = {
    val uri = new URI(url)
    val hash = md5(if (uri.getQuery != null) s"${uri.getPath}?${uri.getQuery}" else uri.getPath)
    s"${uri.getHost}/$hash"
  }
  private def md5(text: String): String = {
    import java.security.MessageDigest
    val digest = MessageDigest.getInstance("MD5")
    digest.digest(text.getBytes).map("%02x".format(_)).mkString
  }
}

class MongodbJsonStore extends JsonStore {
  val json = Mongodb.collection("mongodb.collection.jsonstore")
  override def write(jsons: List[JObject]) = {
    val req = jsons.map { json => Document(compact(render(json))) }.toSeq
    json.insertMany(req).head.map(a => Unit)
  }
  override def count: Future[Long] = json.count.head
  override def empty = Mongodb.empty(json)
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

  def empty(collection: MongoCollection[Document]): Future[Unit] = {
    val result = Promise[Unit]
    for (q <- collection.deleteMany(Document()).head) {
      result.success(Unit)
    }
    result.future
  }

  def parseCrawlResponse(doc: Document) = {
    val status = doc.get("status").get.asInt32().getValue
    val request = parseCrawlRequest(Document(doc.get("request").get.asDocument()))
    val headers = for {
      a <- doc.get("headers").get.asDocument().entrySet()
    } yield (a.getKey -> a.getValue.asArray().map(b => b.asString().getValue).toList)
    val response = getString(doc, "response")
    val created = doc.get("created").get.asInt64().getValue
    CrawlResponse(request, status, headers.toMap, response, created)
  }

  def parseCrawlRequest(doc: Document) = {
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
