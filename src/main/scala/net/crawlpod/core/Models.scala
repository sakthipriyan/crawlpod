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

  def toJsonString = compact(render(toJson))

  def toJson = ("url" -> url) ~
    ("extractor" -> extractor) ~
    ("method" -> method) ~
    ("headers" -> headers) ~
    ("requestBody" -> requestBody) ~
    ("passData" -> passData) ~
    ("cache" -> cache)

}

case class CrawlResponse(
    request: CrawlRequest,
    status: Int,
    headers: Map[String, List[String]],
    body: String,
    created: Long = System.currentTimeMillis,
    timeTaken: Int = -1) {
  def toDom = Jsoup.parse(body)
  def toJson = parse(body).asInstanceOf[JObject]
  def toXml = XML.loadString(body)
  def toJsonString = compact(render(("request" -> request.toJson) ~
    ("status" -> status) ~ ("headers" -> headers) ~
    ("response" -> body) ~ ("created" -> created) ~
    ("timeTaken" -> timeTaken)))
}

case class Extract(
    documents: List[JObject] = List[JObject](),
    requests: List[CrawlRequest] = List[CrawlRequest]()) {
  def this(document: JObject) = this(List[JObject](document), List[CrawlRequest]())
  def this(request: CrawlRequest) = this(List[JObject](), List[CrawlRequest](request))
}
