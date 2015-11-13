package net.crawlpod.external

import scala.collection.JavaConversions.asScalaBuffer
import scala.concurrent.ExecutionContext.Implicits.global

import org.json4s.JsonAST.JObject

import com.typesafe.config.ConfigFactory

import net.crawlpod.core.CrawlRequest
import net.crawlpod.core.CrawlResponse
import net.crawlpod.core.JsonStore
import net.crawlpod.core.Queue
import net.crawlpod.core.RawStore

import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.Document

/**
 * @author sakthipriyan
 */

object Mongodb {
  val config = ConfigFactory.load("mongodb")
  val client = MongoClient(config.getString("mongodb.url"))
  val database = client.getDatabase("mydb")
  def collection(name: String) = database.getCollection(config.getString(name))
  def shutdown = client.close
  def test = {
      
  }
}

class MongodbQueue extends Queue {
  val queue = Mongodb.collection("mongodb.collection.queue")

  override def enqueue(requests: List[CrawlRequest]) = {
    val req = requests.map(r => Document(r.toJsonString)).toSeq
    val completed = queue.insertMany(req)
    
  }
  
  override def dequeue: Option[CrawlRequest] = {
    Some(CrawlRequest("http://google.com", "net.crawlpod.extract.Google"))
  }
  override def failed(req: CrawlRequest, res: CrawlResponse) = {}
  override def size: Long = 0
  override def completed: Long = 0
  override def empty = {}
  override def shutdown = Mongodb.shutdown
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
