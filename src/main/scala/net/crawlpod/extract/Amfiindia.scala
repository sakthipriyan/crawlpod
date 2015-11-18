package net.crawlpod.extract

import net.crawlpod.core.CrawlResponse
import net.crawlpod.core.Extract
import net.crawlpod.core.CrawlRequest

/**
 * @author sakthipriyan
 * https://www.amfiindia.com/net-asset-value/nav-history
 *
 */
class Amfiindia {
  def navHistoryOn(response: CrawlResponse): Extract = {
    val doc = response.toDom
    Extract()
  }

  def init(response: CrawlResponse): Extract = {
    val headers = Some(Map(
      "Referer" -> "https://www.amfiindia.com/net-asset-value/nav-history",
      "Origin" -> "https://www.amfiindia.com",
      "X-Requested-With" -> "XMLHttpRequest",
      "User-Agent" -> "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36",
      "Content-Type" -> "application/x-www-form-urlencoded; charset=UTF-8"))
    val body = Some("fDate=27-Oct-2015")
    val url = "https://www.amfiindia.com/modules/NavHistoryAll"
    val method = "net.crawlpod.extract.Amfiindia.navHistoryOn"
    val request = CrawlRequest(url, method, "POST", headers, None, body)
    new Extract(request)
  }
}