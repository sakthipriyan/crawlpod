package net.crawlpod.driver

import org.scalatest.WordSpec
import com.typesafe.config.ConfigFactory
import net.crawlpod.core.Queue
import net.crawlpod.core.CrawlRequest
import net.crawlpod.UnitSpec
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.time.Span
import org.scalatest.time.Minutes
import org.scalatest.time.Millis
import net.crawlpod.core.RawStore
import net.crawlpod.core.JsonStore
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.JsonAST._
import org.json4s.native.JsonMethods._
import net.crawlpod.core.Http

/**
 * @author sakthipriyan
 */
class HttpSpec extends UnitSpec {

  val http = Http(config.getString("crawlpod.provider.http"))
  "Http" when {
    "crawled" should {
      "succeed" in {
        val request = CrawlRequest("http://sakthipriyan.com", "net.crawlpod.extract.Sakthipriyan")
        whenReady(http.crawl(request)) {
          response => response.status should be(200)
        }
      }
    }
  }
}