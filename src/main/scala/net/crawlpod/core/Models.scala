package net.crawlpod.core

import scala.xml.XML
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.JsonAST._
import org.json4s.native.JsonMethods._
import org.jsoup.Jsoup
import java.net.URI
import net.crawlpod.util.CryptoUtil

case class Enqueue(requests: List[CrawlRequest])
case class JsonWrite(list: List[JObject])
case class Failed(request: CrawlRequest)
case class MarkProcessed(request: CrawlRequest)
case class Process(request: CrawlRequest)

case object Start
case object Stop
case object Tick
case object Stats
case object Dequeue

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

  lazy val id = {
    val uri = new URI(url)
    val sb = new scala.collection.mutable.StringBuilder
    sb ++= uri.getPath
    if (uri.getQuery != null) {
      sb ++= "?"
      sb ++= uri.getQuery
    }
    for (body <- requestBody) {
      sb ++= body
    }
    val hash = CryptoUtil.md5(sb.toString)
    s"${uri.getHost}/$hash"
  }

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
    
  override def toString = {
    val sizeSuffix = if(body.length > 10) (10,"...") else (body.length,"") 
    s"CrawlResponse($request,$status,$headers,${body.substring(0,sizeSuffix._1)}${sizeSuffix._2},$created,$timeTaken)"
  }
}

case class Extract(
    documents: List[JObject] = List[JObject](),
    requests: List[CrawlRequest] = List[CrawlRequest]()) {
  def this(document: JObject) = this(List[JObject](document), List[CrawlRequest]())
  def this(request: CrawlRequest) = this(List[JObject](), List[CrawlRequest](request))
}
