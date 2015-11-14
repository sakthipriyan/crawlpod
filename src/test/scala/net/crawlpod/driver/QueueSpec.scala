package net.crawlpod.driver

import org.scalatest.WordSpec
import com.typesafe.config.ConfigFactory
import net.crawlpod.core.Queue
import net.crawlpod.core.CrawlRequest
import net.crawlpod.UnitSpec

/**
 * @author sakthipriyan
 */
class QueueSpec extends UnitSpec {
  
  val config = ConfigFactory.load()
  val queue = Queue(config.getString("crawlpod.provider.queue"))
  
  "A Queue" when {
    "enqueued" should {
      "succeed" in {
        queue.enqueue(List(CrawlRequest("http://google.com","net.crawlpod.extract.Google")))
      }
    }
  }
}