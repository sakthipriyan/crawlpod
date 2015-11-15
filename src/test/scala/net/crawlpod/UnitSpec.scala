package net.crawlpod

import org.scalatest.Inside
import org.scalatest.Inspectors
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures

import com.typesafe.config.ConfigFactory

abstract class UnitSpec extends WordSpec with Matchers with
  OptionValues with Inside with Inspectors with ScalaFutures with Config

trait Config {
  val config = ConfigFactory.load("application-test")
}