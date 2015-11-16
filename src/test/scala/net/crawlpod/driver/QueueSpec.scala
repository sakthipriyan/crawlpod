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

/**
 * @author sakthipriyan
 */
class QueueSpec extends UnitSpec {

  val queue = Queue(config.getString("crawlpod.provider.queue"))

  "Queue" when {
    "cleared" should {
      "succeed" in {
        whenReady(queue.empty) {
          s => s should be(())
        }
      }
    }
    "enqueued" should {
      "succeed" in {
        val future = queue.enqueue(List(
          CrawlRequest("http://google.com", "net.crawlpod.extract.Google"),
          CrawlRequest("http://google.com", "net.crawlpod.extract.Google",
            headers = Some(Map("Test" -> "Hello")),
            passData = Some(Map("Test" -> "Hello"))),
          CrawlRequest("http://google.com", "net.crawlpod.extract.Google", "POST", requestBody = Some("Hello"))))
        whenReady(future) {
          s => s should be(())
        }
      }
    }
    "counted" should {
      "return count 3" in {
        val size = queue.queueSize
        whenReady(size) {
          s => assert(s == 3)
        }
      }
    }

    "dequeued 1st " should {
      "return CrawlRequest without headers and passData" in {
        whenReady(queue.dequeue) {
          r =>
            {
              assert(r.isDefined)
              assert(r.get.headers.isEmpty)
              assert(r.get.passData.isEmpty)
            }
        }
      }
    }

    "dequeued 2nd " should {
      "return CrawlRequest with headers and passData" in {
        whenReady(queue.dequeue) {
          r =>
            {
              assert(r.isDefined)
              assert(r.get.headers.isDefined)
              assert(r.get.passData.isDefined)
            }
        }
      }
    }

    "dequeued 3rd " should {
      "return CrawlRequest without headers and passData but with requestBody" in {
        whenReady(queue.dequeue) {
          r =>
            {
              assert(r.isDefined)
              assert(r.get.headers.isEmpty)
              assert(r.get.passData.isEmpty)
              assert(r.get.requestBody.isDefined)
            }
        }
      }
    }

    "dequeued 4th " should {
      "return None" in {
        whenReady(queue.dequeue) {
          r =>
            {
              assert(r.isEmpty)
            }
        }
      }
    }

    "now count doneSize" should {
      "return 3" in {
        whenReady(queue.doneSize) {
          r =>
            {
              assert(r == 3)
            }
        }
      }
    }

    "failed" should {
      "succeed" in {
        whenReady(queue.failed(CrawlRequest("http://sakthipriyan.com", "net.crawlpod.extract.Sakthipriyan"))) {
          s => s should be(())
        }
      }
    }

    "failedSize" should {
      "return 1 " in {
        whenReady(queue.failedSize) {
          s => assert(1 == s)
        }
      }
    }
  }
}