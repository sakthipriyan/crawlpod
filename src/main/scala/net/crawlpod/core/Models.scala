package net.crawlpod.core 

case class CrawlRequest()
case class CrawlResponse()
case class Enqueue(requests:List[CrawlRequest])
case class Dequeue(count:Int)
