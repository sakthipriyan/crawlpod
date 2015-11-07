package net.crawlpod.core 

import org.json4s.JsonAST.JObject

case class CrawlRequest()
case class CrawlResponse()

case class Enqueue(requests:List[CrawlRequest])
case class Dequeue(count:Int)

case class JsonWrite(json: List[JObject])

case class RawStoreWrite()
case class RawStoreRead()

case object Start
case object Stop
case object Tick
