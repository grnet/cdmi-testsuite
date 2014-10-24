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
import gr.grnet.cdmi.client.conf.{ClassTestConf, ConfKey, ProfileConf, SuiteConf}
import gr.grnet.cdmi.client.business._

import scala.annotation.tailrec

/**
 *
 */
object Main {
  type HeaderKeyValue = (String, String)

  @tailrec
  final def runTestCases(clientFactory: () ⇒ Client, testCases: List[(TestCase, ClassTestConf)]): Unit = {
    testCases match {
      case (testCase, conf) :: remaining ⇒
        val client = clientFactory()
        testCase(client, conf)
        runTestCases(clientFactory, remaining)

      case _ ⇒
    }
  }

  def main(profileConf: ProfileConf): Unit = {
    val ok = new OkHttpClient
    val clientFactory = () ⇒ new Client(profileConf, ok)

    val effectiveClassTests = profileConf.effectiveClassTests

    object ClassTestsCheck extends TestCaseSkeleton {
      override def description = s"Check availability of classes from `${ConfKey.`class-tests`}`"
      val stepsF = () ⇒
        for {
          effectiveClassTest ← effectiveClassTests
          fqClassName = effectiveClassTest.testClassName
        } yield {
          TestStep.effect(s"class $fqClassName can be instantiated as a ${classOf[TestCase].getSimpleName}")(Class.forName(fqClassName).newInstance().asInstanceOf[TestCase])
        }

      def steps = stepsF()
    }

    ClassTestsCheck() match {
      case TestCasePassed ⇒
        // continue ONLY if all test classes can be instantiated
        val testCases =
          for {
            classTestConf ← effectiveClassTests
            fqClassName = classTestConf.testClassName
            theClass = Class.forName(fqClassName)
            testCase = theClass.newInstance().asInstanceOf[TestCase]
          } yield {
            testCase → classTestConf
          }

        runTestCases(clientFactory, testCases)

      case _ ⇒
        sys.exit(5)
    }
  }

  def main(options: Args.GlobalOptions): Unit = {
    val conf = options.conf
    val profile = options.profile
    val xconf = options.xconf

    val refConfig = ConfigFactory.parseResources("reference.conf").resolve()

    // Parse and validate -c
    val cConfigF = () ⇒
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
      def steps = List(TestStep.effect("Check provided configuration")(cConfigF()))
    }
    MasterConfCheck() match {
      case TestCaseNotPassed(_, _) ⇒
        sys.exit(2)

      case _ ⇒
    }

    val cConfig = cConfigF()

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
    XConfCheck() match {
      case TestCaseNotPassed(_, _) ⇒
        sys.exit(3)

      case _ ⇒
    }

    val xConfig = xConfigF()

    val profileConfEither = SuiteConf.parse(cConfig, profile, xConfig)
    profileConfEither match {
      case Left(error) ⇒
        System.err.println(error)
        sys.exit(4)

      case Right(profileConf) ⇒
        main(profileConf)
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
