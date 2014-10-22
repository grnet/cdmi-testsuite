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

import com.beust.jcommander.ParameterException
import com.squareup.okhttp.OkHttpClient
import com.typesafe.config._
import gr.grnet.cdmi.client.cmdline.Args
import gr.grnet.cdmi.client.cmdline.Args.ParsedCmdLine
import gr.grnet.cdmi.client.testmodel._

import scala.collection.JavaConverters._

/**
 *
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

  def main(options: Args.GlobalOptions): Unit = {
    val conf = options.conf
    val profile = options.profile
    val xconf = options.xconf
    println("conf = " + conf)
    println("profile = " + profile)
    println("xconf = " + xconf)

    // Parse and validate -c
    val configF = () ⇒
      conf match {
        case "default" ⇒
          ConfigFactory.parseResources("reference.conf").resolve()

        case path ⇒
          ConfigFactory.parseFile(new File(path).getAbsoluteFile).resolve()
      }

    object MasterConfCheck extends TestCaseSkeleton {
      override def description: String = s"Master configuration exists [$profile]"
      def steps = List(TestStep.effect("Check provided configuration")(configF()))
    }
    MasterConfCheck.apply(TestConfig.Empty, () ⇒ null) match {
      case TestCaseNotPassed(_, _) ⇒
        sys.exit(2)

      case _ ⇒
    }

    val config = configF()

    val profileConfigF = () ⇒ configF().getConfig(s"${ConfKey.profiles}.$profile")
    val profileHttpHeadersF = () ⇒ profileConfigF().getStringList(ConfKey.`http-headers`)

    val globalF      = () ⇒ configF().getConfig(ConfKey.global)
    val rootUriF     = () ⇒ globalF().getString(ConfKey.CDMI_ROOT_URI)
    val httpHeadersF = () ⇒ globalF().getConfig(ConfKey.`http-headers`)
    val specVersionF = () ⇒ httpHeadersF().getString(ConfKey.`X-CDMI-Specification-Version`)
    val classTestsF  = () ⇒ configF().getConfig(ConfKey.`class-tests`)
    val shellTestsF  = () ⇒ configF().getConfig(ConfKey.`shell-tests`)

    val ok = new OkHttpClient
    val clientFactory = () ⇒ new HttpClient(rootUriF(), ok, httpHeadersF())

    val fqClassNamesF = () ⇒ classTestsF().root().keySet().asScala.toList

    // Validate configuration I
    object ConfigurationCheck extends TestCaseSkeleton {
      def steps = List(
        TestStep.effect(s"Provided profile $profile exists"              )(profileConfigF()),
        TestStep.effect(s"${ConfKey.`http-headers`} in the provided profile $profile exist")(httpHeadersF()),
        TestStep.effect( "`global` exists in configuration"              )(globalF()),
        TestStep.effect( "`global.CDMI_ROOT_URI` exists in configuration")(rootUriF()),
        TestStep.effect( "`global.http-headers` exists in configuration" )(httpHeadersF()),
        TestStep.effect( "`global.http-headers.X-CDMI-Specification-Version` exists in configuration")(specVersionF()),
        TestStep.effect( "`class-tests` exists in configuration")(classTestsF()),
        TestStep.effect( "`shell-tests` exists in configuration")(shellTestsF())
      )
    }

    // Validate configuration II
    object ClassTestsCheck extends TestCaseSkeleton {
      override def description = s"Check availability of classes from `${ConfKey.`class-tests`}`"
      val stepsF = () ⇒
        for {
          fqClassName ← fqClassNamesF()
        } yield {
          TestStep.effect(s"class $fqClassName can be instantiated as a ${classOf[TestCase].getSimpleName}")(Class.forName(fqClassName).newInstance().asInstanceOf[TestCase])
        }

      def steps = stepsF()
    }

    val testConfig = TestConfig(config, ConfigFactory.empty())
    ConfigurationCheck.apply(testConfig, clientFactory)  match {
      case TestCaseNotPassed(_, _) ⇒
        sys.exit(3)

      case _ ⇒
    }

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

            // The configuration, using-c in the command line
            val localConfig = classTests.getConfig(id)
            // Overridden configuration, using -x from the command line
//            val overridenLocalConfig = localConfig.withValue(ConfKey.`http-headers`, profileConfigF().v)

            (theTest, localConfig)
          }

        runTestCases(globalConfig, testCases, clientFactory)

      case _ ⇒
        sys.exit(4)
    }
  }

  def main(args: Array[String]): Unit = {
    val jc = Args.jc
    try {
      jc.parse(args: _*)

      val options = ParsedCmdLine.globalOptions
      if(options.help) {
        jc.usage()
      }
      else {
        main(options)
      }
    }
    catch {
      case e: ParameterException ⇒
        System.err.println(e.getMessage)
        sys.exit(1)

      case e: Exception ⇒
        e.printStackTrace(System.err)
        sys.exit(2)

      case e: Throwable ⇒
        e.printStackTrace(System.err)
        sys.exit(3)
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
  }
}
