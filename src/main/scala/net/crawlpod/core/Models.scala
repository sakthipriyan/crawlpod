package net.crawlpod.core

import scala.xml.XML

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.JsonAST._
import org.json4s.native.JsonMethods._

import org.jsoup.Jsoup

case class Enqueue(requests: List[CrawlRequest])
case class Dequeue()

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

  def toJsonString = {
    compact(render(toJson))
  }

  def toJson = {
    ("url" -> url) ~
      ("extractor" -> extractor) ~
      ("method" -> method) ~
      ("headers" -> headers) ~
      ("passData" -> passData) ~
      ("requestBody" -> requestBody) ~
      ("cache" -> cache)
  }
}

case class CrawlResponse(
    status: Int,
    request: CrawlRequest,
    headers: Seq[(String, String)],
    response: String,
    created: Long = System.currentTimeMillis) {
  def toDom = Jsoup.parse(response)
  def toJson = parse(response).asInstanceOf[JObject]
  def toXml = XML.loadString(response)
  def toJsonString = compact(render(("status" -> status) ~
    ("request" -> request.toJson) ~
    ("headers" -> headers) ~
    ("response" -> response) ~
    ("created" -> created)))
}

case class Extract(
    documents: List[JObject] = List[JObject](),
    requests: List[CrawlRequest] = List[CrawlRequest]()) {
  def this(document: JObject) = this(List[JObject](document), List[CrawlRequest]())
  def this(request: CrawlRequest) = this(List[JObject](), List[CrawlRequest](request))
}
