package net.crawlpod.driver

import org.scalatest.WordSpec
import com.typesafe.config.ConfigFactory
import net.crawlpod.core.Queue
import net.crawlpod.core.CrawlRequest
import net.crawlpod.UnitSpec
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author sakthipriyan
 */
class QueueSpec extends UnitSpec {

  val queue = Queue(config.getString("crawlpod.provider.queue"))

  "A Queue" when {
    "enqueued" should {
      "succeed" in {
        queue.enqueue(List(
          CrawlRequest("http://google.com", "net.crawlpod.extract.Google"),
          CrawlRequest("http://google.com", "net.crawlpod.extract.Google"),
          CrawlRequest("http://google.com", "net.crawlpod.extract.Google")))
      }
    }
    "count" should {
      "return count" in {
        val size = queue.size
        for(s <- size){
          println(s)
        }
        println("hello")
      }
    }
  }
}