package net.crawlpod

import org.scalatest.Inside
import org.scalatest.Inspectors
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures
import com.typesafe.config.ConfigFactory
import org.scalatest.time.Minutes
import org.scalatest.time.Millis
import org.scalatest.time.Span

//FlatSpec with Matchers
abstract class UnitSpec extends WordSpec with Matchers with
  OptionValues with Inside with Inspectors with ScalaFutures  {
  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Minutes), interval = Span(500, Millis))
  val config = ConfigFactory.load("application-test")
}

