package net.crawlpod

/**
 * @author sakthipriyan
 */

import org.scalatest._
import com.typesafe.config.ConfigFactory

abstract class UnitSpec extends WordSpec with Matchers with
  OptionValues with Inside with Inspectors { 
    val config = ConfigFactory.load("application-test")
}