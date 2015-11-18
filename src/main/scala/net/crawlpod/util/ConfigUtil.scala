package net.crawlpod.util

import com.typesafe.config.ConfigFactory

/**
 * @author sakthipriyan
 */
object ConfigUtil {

  private val cfg = ConfigFactory.load()
  val isCacheEnabled = cfg.getBoolean("app.cache.enabled")
  val afterTs = if (cfg.getBoolean("startup.new")) System.currentTimeMillis else cfg.getLong("app.cache.ts") * 1000
  val httpProvider = cfg.getString("crawlpod.provider.http")
  val queueProvider = cfg.getString("crawlpod.provider.queue")
  val rawStoreProvider = cfg.getString("crawlpod.provider.rawstore")
  val jsonStoreProvider = cfg.getString("crawlpod.provider.jsonstore")
  val requestStoreProvider = cfg.getString("crawlpod.provider.requeststore")

}