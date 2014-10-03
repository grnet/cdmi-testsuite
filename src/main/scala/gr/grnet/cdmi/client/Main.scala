/*
 * Copyright (C) 2014 GRNET S.A.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package gr.grnet.cdmi.client

import java.io.File

import com.squareup.okhttp.OkHttpClient
import com.typesafe.config._
import gr.grnet.cdmi.client.testmodel._

import scala.collection.JavaConverters._

/**
 *
 * @author Christos KK Loverdos <loverdos@gmail.com>
 */
object Main {
  type HeaderKeyValue = (String, String)

  def readHeadersFromEnv(): List[HeaderKeyValue] = {
    def loop(id: Int, acc: List[HeaderKeyValue]): List[HeaderKeyValue] = {
      System.getenv("CDMI_HEADER_" + id) match {
        case null ⇒
          acc

        case h ⇒
          val Array(key, value) = h.split(':')
          loop(id + 1, (key.trim, value.trim) :: acc)
      }
    }

    loop(0, Nil) match {
      case Nil ⇒
        loop(1, Nil)
      case acc ⇒
        acc
    }
  }

  def runTestCases(
    globalConfig: Config,
    testCases: List[(TestCase, Config)],
    clientFactory: () ⇒ HttpClient
  ): Unit = {
    testCases match {
      case (testCase, localConfig) :: remaining ⇒
        val config = TestConfig(globalConfig, localConfig)
        testCase.apply(config, clientFactory)
        runTestCases(globalConfig, remaining, clientFactory)

      case _ ⇒
    }
  }

  def main(args: Array[String]): Unit = {
    val config0 =
      args match {
        case Array() ⇒
          // No argument, mean we load from
          ConfigFactory.load(
            Thread.currentThread().getContextClassLoader,
            ConfigResolveOptions.noSystem()
          )

          ConfigFactory.load(
            ConfigFactory.empty(),
            ConfigResolveOptions.noSystem()
          )

          ConfigFactory.parseResources("reference.conf")

        case Array(path) ⇒
          ConfigFactory.parseFile(new File(path).getAbsoluteFile)

        case _ ⇒
          System.err.println(s"Usage: Main [CONF_FILE_PATH]")
          sys.exit(1)
      }

    val envHttpHeaders = readHeadersFromEnv()
    def overrideConfigHeaders(config: Config, headers: List[HeaderKeyValue]): Config =
      headers match {
        case Nil ⇒
          config

        case (key, value) :: tailHeaders ⇒
          val keyPath = s"global.http-headers.$key"
          val newConfig = config.withValue(keyPath, ConfigValueFactory.fromAnyRef(value))
          overrideConfigHeaders(newConfig, tailHeaders)
      }

    val config       = overrideConfigHeaders(config0, envHttpHeaders).resolve()

    val globalF      = () ⇒ config.getConfig("global")
    val rootUriF     = () ⇒ globalF().getString("CDMI_ROOT_URI")
    val httpHeadersF = () ⇒ globalF().getConfig("http-headers")
    val specVersionF = () ⇒ httpHeadersF().getString("X-CDMI-Specification-Version")
    val classTestsF  = () ⇒ config.getConfig("class-tests")
    val shellTestsF  = () ⇒ config.getConfig("shell-tests")

    val ok = new OkHttpClient
    val clientFactory = () ⇒ new HttpClient(rootUriF(), ok, httpHeadersF())

    val fqClassNamesF = () ⇒ classTestsF().root().keySet().asScala.toList

    // eating our own dog food
    object ConfigurationCheck extends TestCaseSkeleton {
      def steps = List(
        TestStep.effect("`global` exists in configuration"              )(globalF()),
        TestStep.effect("`global.CDMI_ROOT_URI` exists in configuration")(rootUriF()),
        TestStep.effect("`global.http-headers` exists in configuration" )(httpHeadersF()),
        TestStep.effect("`global.http-headers.X-CDMI-Specification-Version` exists in configuration")(specVersionF()),
        TestStep.effect("`class-tests` exists in configuration")(classTestsF()),
        TestStep.effect("`shell-tests` exists in configuration")(shellTestsF())
      )
    }

    // also eating our own dog food
    object ClassTestsCheck extends TestCaseSkeleton {
      override def description = "Check availability of classes from `class-tests`"
      val stepsF = () ⇒
        for {
          fqClassName ← fqClassNamesF()
        } yield {
          TestStep.effect(s"class $fqClassName can be instantiated as a ${classOf[TestCase].getSimpleName}")(Class.forName(fqClassName).newInstance().asInstanceOf[TestCase])
        }

      def steps = stepsF()
    }

    val testConfig = TestConfig(config, ConfigFactory.empty())
    ConfigurationCheck.apply(testConfig, clientFactory)
    ClassTestsCheck   .apply(testConfig, clientFactory) match {
      case TestCasePassed ⇒
        // continue ONLY if all test classes can be instantiated
        val fqClassNames = fqClassNamesF()
        val classTests   = classTestsF()
        val globalConfig = globalF()

        val testCases =
          for {
            fqClassName ← fqClassNames
            theClass = Class.forName(fqClassName)
            theTest = theClass.newInstance().asInstanceOf[TestCase]
          } yield {

            val id = (new StringBuilder).append('"').append(fqClassName).append('"').toString()
            val localConfig = classTests.getConfig(id)

            (theTest, localConfig)
          }

        runTestCases(globalConfig, testCases, clientFactory)

      case _ ⇒
    }
  }
}
