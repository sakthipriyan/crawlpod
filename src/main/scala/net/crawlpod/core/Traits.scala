package net.crawlpod.core

import org.json4s.JsonAST.JObject

/**
 * @author sakthipriyan
 */

trait Queue {
  def enqueue(r:List[CrawlRequest])
  def dequeue: CrawlRequest
  def failed(req:CrawlRequest, res:CrawlResponse)
  def size: Long
  def completed : Long
}

trait RawStore {
  def store(id: String, r: CrawlResponse)
  def retrieve(id: String): CrawlResponse
}

trait RawStoreIndex {
  def cache(id: String)
  def inCache(id: String): Boolean
  def count: Long
}

trait DocumentStore {
  def write(json: List[JObject])
  def read(key:String) : JObject
  def count: Long
}

trait Empty {
  def empty
}

trait Shutdown {
  def shutdown
}
