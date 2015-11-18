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
import net.crawlpod.core.RequestStore

/**
 * @author sakthipriyan
 */
class RequestStoreSpec extends UnitSpec {

  val requestStore = RequestStore(config.getString("crawlpod.provider.requeststore"))
  val request = CrawlRequest("http://sakthipriyan.com","crawlpod.Sakthipriyan")
  "RequestStore" when {
    "emptied" should {
      "succeed" in {
        whenReady(requestStore.empty) {
          s => s should be(())
        }
      }
    }
    "count" should {
      "return 0" in {
        whenReady(requestStore.count) {
          s => s should be(0)
        }
      }
    }
    "isProcessed before" should {
      "return false" in {
        whenReady(requestStore.isProcessed(request)) {
          s => s should be(false)
        }
      }
    }
    val timestamp = System.currentTimeMillis
    "setProcessed" should {
      "succeed" in {
        whenReady(requestStore.setProcessed(request,timestamp)) {
          s => s should be(())
        }
      }
    }
    "count" should {
      "return 1" in {
        whenReady(requestStore.count) {
          s => s should be(1)
        }
      }
    }
    "isProcessed after" should {
      "return true" in {
        whenReady(requestStore.isProcessed(request,timestamp-1000)) {
          s => s should be(true)
        }
      }
    }
    "isProcessed after future ts" should {
      "return false" in {
        whenReady(requestStore.isProcessed(request,timestamp+1000)) {
          s => s should be(false)
        }
      }
    }
    
    "setProcessed with new ts" should {
      "return succeed" in {
        whenReady(requestStore.setProcessed(request,timestamp+2000)) {
          s => s should be(())
        }
      }
    }
    "isProcessed after future ts again" should {
      "return false" in {
        whenReady(requestStore.isProcessed(request,timestamp)) {
          s => s should be(true)
        }
      }
    }
  }
}