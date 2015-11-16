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

/**
 * @author sakthipriyan
 */
class JsonStoreSpec extends UnitSpec {

  val json = JsonStore(config.getString("crawlpod.provider.jsonstore"))
  "JsonStore" when {
    "emptied" should {
      "succeed" in {
        whenReady(json.empty) {
          s => s should be(())
        }
      }
    }
    "count" should {
      "return 0" in {
        whenReady(json.count) {
          s => s should be(0)
        }
      }
    }
    "write" should {
      "succeed" in {
        val jsons = List((("_id"->"1")~("data"->234)),(("_id"->"2")~("data"->234)))
        whenReady(json.write(jsons)) {
          s => s should be(())
        }
      }
    }
    "count" should {
      "return 2 now" in {
        whenReady(json.count) {
          r => assert(r == 2)
        }
      }
    }
  }
}