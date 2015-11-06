package net.crawlpod.core

/**
 * @author sakthipriyan
 */
abstract class RawStore {
  def store(id: String, r: CrawlResponse)
  def retrieve(id: String): CrawlResponse
}

abstract class RawStoreIndex {
  def cache(id: String)
  def inCache(id: String): Boolean
  def count: Long
}

abstract class DocumentStore {
  // TODO to appropriate json class (next 2 lines)
  def write(json: List[String])
  def count: Long
}

trait Empty {
  def empty
}

trait Shutdown {
  
}

