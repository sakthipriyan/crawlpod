package net.crawlpod.core.client

import net.crawlpod.core.CrawlRequest
import net.crawlpod.core.CrawlResponse
import net.crawlpod.core.Empty
import net.crawlpod.core.Queue
import net.crawlpod.core.Shutdown
import net.crawlpod.core.RawStore
import net.crawlpod.core.JsonStore
import org.json4s.JsonAST.JObject
import net.crawlpod.core.CrawlRequest
import net.crawlpod.core.CrawlRequest

/**
 * @author sakthipriyan
 */
class MongodbQueue extends Queue with Empty with Shutdown {
  override def enqueue(r: List[CrawlRequest]) = {

  }
  override def dequeue: Option[CrawlRequest] = {
    Some(CrawlRequest("http://google.com","net.crawlpod.extract.Google"))
  }
  override def failed(req: CrawlRequest, res: CrawlResponse) = {}
  override def size: Long = 0
  override def completed: Long = 0
  override def empty = {}
  override def shutdown = {}
}

class MongodbRawStore extends RawStore with Empty with Shutdown {
  override def put(res: CrawlResponse) = {}
  override def get(req: CrawlRequest): Option[CrawlResponse] = None
  override def count: Long = 0
  override def empty = {}
  override def shutdown = {}

}

class MongodbJsonStore extends JsonStore with Empty with Shutdown {
  override def write(json: List[JObject]) = {}
  override def count: Long = 0
  override def empty = {}
  override def shutdown = {}
}
