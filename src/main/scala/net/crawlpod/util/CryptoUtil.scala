package net.crawlpod.util

/**
 * @author sakthipriyan
 */
object CryptoUtil {
  def md5(text: String): String = {
    import java.security.MessageDigest
    val digest = MessageDigest.getInstance("MD5")
    digest.digest(text.getBytes).map("%02x".format(_)).mkString
  }
}