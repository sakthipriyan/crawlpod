package net.crawlpod.core

import org.json4s.JsonAST.JObject

/**
 * @author sakthipriyan
 */

trait Queue {
  def enqueue(r:List[CrawlRequest])
  def dequeue: Option[CrawlRequest]
  def failed(req:CrawlRequest, res:CrawlResponse)
  def size: Long
  def completed : Long
}

trait RawStore {
  def put(res: CrawlResponse)
  def get(req: CrawlRequest): Option[CrawlResponse]
  def count: Long
}

trait JsonStore {
  def write(json: List[JObject])
  def count: Long
}

trait Empty {
  def empty
}

trait Shutdown {
  def shutdown
}
