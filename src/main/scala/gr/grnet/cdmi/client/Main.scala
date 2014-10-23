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
import gr.grnet.cdmi.client.conf.ConfKey
import gr.grnet.cdmi.client.testmodel._

import scala.collection.JavaConverters._

/**
 *
 */
object Main {
  type HeaderKeyValue = (String, String)

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

    val refConfig = ConfigFactory.parseResources("reference.conf").resolve()

    // Parse and validate -c
    val configF = () ⇒
      conf match {
        case "default" ⇒
          refConfig

        case path ⇒
          val config = ConfigFactory.parseFile(new File(path).getAbsoluteFile).resolve()
          config.checkValid(
            refConfig,
            "global", "profiles", "class-tests", "class-tests-list", "shell-tests"
          )
          config
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

    // Parse and validate -x
    val xConfigF = () ⇒
      if(xconf.startsWith("@")) {
        ConfigFactory.parseFile(new File(xconf.substring(1)).getAbsoluteFile).resolve()
      }
      else {
        ConfigFactory.parseString(xconf).resolve()
      }

    object XConfCheck extends TestCaseSkeleton {
      override def description: String = s"Option -x"
      def steps = List(TestStep.effect("Check parameter for -x")(xConfigF()))
    }
    XConfCheck.apply(TestConfig.Empty, () ⇒ null) match {
      case TestCaseNotPassed(_, _) ⇒
        sys.exit(3)

      case _ ⇒
    }

    val xConfig = xConfigF()

    val profileConfigF = () ⇒ configF().getConfig(s"${ConfKey.profiles}.$profile")
    val profileHttpHeadersListF = () ⇒ profileConfigF().getStringList(ConfKey.`http-headers-list`)

    val globalF  = () ⇒ configF().getConfig(ConfKey.global)
    val globalRootUriF = () ⇒ globalF().getString(ConfKey.CDMI_ROOT_URI)
    val globalHttpHeadersF = () ⇒ globalF().getConfig(ConfKey.`http-headers`)
    val globalSpecVersionF = () ⇒ globalHttpHeadersF().getString(ConfKey.`X-CDMI-Specification-Version`)
    val classTestsF = () ⇒ configF().getConfig(ConfKey.`class-tests`)
    val shellTestsF = () ⇒ configF().getConfig(ConfKey.`shell-tests`)

    val ok = new OkHttpClient
    val clientFactory = () ⇒ new HttpClient(globalRootUriF(), ok, globalHttpHeadersF())

    val fqClassNamesF = () ⇒ classTestsF().root().keySet().asScala.toList

    // Validate configuration I
    object ConfigurationCheck extends TestCaseSkeleton {
      override def description: String = s"Master configuration is valid"

      def steps = List(
        TestStep.effect(s"Provided profile $profile exists")(profileConfigF()),
        TestStep.effect(s"${ConfKey.`http-headers`} in the provided profile $profile exist")(globalHttpHeadersF()),
        TestStep.effect( "`global` exists"              )(globalF()),
        TestStep.effect( "`global.CDMI_ROOT_URI` exists")(globalRootUriF()),
        TestStep.effect( "`global.http-headers` exists" )(globalHttpHeadersF()),
        TestStep.effect( "`global.http-headers.X-CDMI-Specification-Version` exists")(globalSpecVersionF()),
        TestStep.effect( "`class-tests` exists")(classTestsF()),
        TestStep.effect( "`shell-tests` exists")(shellTestsF())
      )
    }

    val profileHttpHeadersList = profileHttpHeadersListF()

    val testConfig = TestConfig(config, ConfigFactory.empty())
    ConfigurationCheck.apply(testConfig, clientFactory)  match {
      case TestCaseNotPassed(_, _) ⇒
        sys.exit(4)

      case _ ⇒
    }

    val profileConfig = profileConfigF()

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

            val id = s""""$fqClassName""""
            println("id = " + id)

            // We get the original test-specific configuration (from the -c option in the command line)
            val cTestConfig = classTests.getConfig(id)
            println("cTestConfig = " + cTestConfig)

            // We augment the test-specific configuration with the profile configuration (the former takes precedence)
            // and then with the configuration of the -x option.
            // NOTE that profileConfig contains a `http-headers-list`. This cannot be merged as is, since we need
            //      a `http-headers` object (map) not list.
            // Based on this list
            //   * we select the specified headers from `global.http-headers`
            //   * and override their values with those given in the `http-headers` of the -x option

            // Keys of the -x configuration
            val xKeys = xConfig.entrySet().asScala.map(_.getKey)
            println("xKeys = " + xKeys)

            val profileHeaderNames = profileHttpHeadersList.asScala.toList
            println("profileHeaderNames = " + profileHeaderNames)

            val pTestConfig = cTestConfig.withFallback(profileConfig)

            // Overridden configuration, using -x from the command line
            val xTestConfig = pTestConfig.withFallback(cTestConfig)
            println("xTestConfig = " + xTestConfig)

            (theTest, xTestConfig)

            sys.exit(0)
          }

        runTestCases(globalConfig, testCases, clientFactory)

      case _ ⇒
        sys.exit(5)
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
  }
}
