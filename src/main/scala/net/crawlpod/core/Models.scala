package net.crawlpod.core

import org.json4s.JsonAST.JObject
import java.util.Arrays
import org.jsoup.Jsoup
import org.json4s.native.JsonMethods._
import scala.xml.XML

case class Enqueue(requests: List[CrawlRequest])
case class Dequeue(count: Int = 1)

case class JsonWrite(list: List[JObject])

case object Start
case object Stop
case object Tick

case class CrawlRequest(
  url: String,
  extractor: String,
  method: String = "GET",
  headers: Option[Map[String, String]] = None,
  passData: Option[Map[String, String]] = None,
  requestBody: Option[String] = None,
  cache: Boolean = true)

case class CrawlResponse(
    status : Int,
    request: CrawlRequest,
    headers: Map[String, List[String]],
    body: String) {
    def toDom = Jsoup.parse(body)
    def toJson = parse(body).asInstanceOf[JObject]
    def toXml = XML.loadString(body)
}

case class Extract(
    documents: List[JObject] = List[JObject](),
    requests: List[CrawlRequest] = List[CrawlRequest]()) {
  def this(document: JObject) = this(List[JObject](document), List[CrawlRequest]())
  def this(request: CrawlRequest) = this(List[JObject](), List[CrawlRequest](request))
}
