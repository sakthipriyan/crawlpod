package net.crawlpod.core

import scala.xml.XML

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.JsonAST._
import org.json4s.native.JsonMethods._

import org.jsoup.Jsoup

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
    cache: Boolean = true) {

  def toJson = {
    (("url" -> url) ~
      ("extractor" -> extractor) ~
      ("method" -> method) ~
      ("headers" -> headers) ~
      ("passData" -> passData) ~
      ("requestBody" -> requestBody) ~
      ("cache" -> cache))
  }

  def toJsonString = {
    compact(render(toJson))
  }
}

case class CrawlResponse(
    status: Int,
    request: CrawlRequest,
    headers: Seq[(String, String)],
    response: String) {
  def toDom = Jsoup.parse(response)
  def toJson = parse(response).asInstanceOf[JObject]
  def toXml = XML.loadString(response)
  
}

case class Extract(
    documents: List[JObject] = List[JObject](),
    requests: List[CrawlRequest] = List[CrawlRequest]()) {
  def this(document: JObject) = this(List[JObject](document), List[CrawlRequest]())
  def this(request: CrawlRequest) = this(List[JObject](), List[CrawlRequest](request))
}
