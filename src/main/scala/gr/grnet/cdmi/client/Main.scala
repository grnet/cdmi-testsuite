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
import gr.grnet.cdmi.client.business._
import gr.grnet.cdmi.client.cmdline.Args
import gr.grnet.cdmi.client.cmdline.Args.ParsedCmdLine
import gr.grnet.cdmi.client.conf.{FullConf, Key, TestConf}

import scala.annotation.tailrec

/**
 *
 */
object Main {
  type HeaderKeyValue = (String, String)

  @tailrec
  final def runTestCases(clientFactory: () ⇒ Client, testCases: List[(TestCase, TestConf)]): Unit = {
    testCases match {
      case (testCase, conf) :: remaining ⇒
        val client = clientFactory()
        testCase(client, conf)
        runTestCases(clientFactory, remaining)

      case _ ⇒
    }
  }

  def acceptOrExit(testCase: TestCase, exitCode: Int): Unit =
    testCase() match {
      case TestCaseNotPassed(_, _) ⇒
        sys.exit(exitCode)

      case _ ⇒
    }

  def parseConf(conf: String): Config = {
    lazy val refConfig = ConfigFactory.parseResources("reference.conf").resolve()

    // Parse and validate -c
    val cConfigF = () ⇒
      conf match {
        case "default" ⇒
          refConfig

        case path ⇒
          val config = ConfigFactory.parseFile(new File(path).getAbsoluteFile).resolve()
          config.checkValid(refConfig)
          config
      }

    object MasterConfCheck extends TestCaseSkeleton(true) {
      override def description: String = s"Master configuration exists"
      def steps = List(TestStep.effect("Check provided configuration")(cConfigF()))
    }
    acceptOrExit(MasterConfCheck, 2)

    cConfigF()
  }
  
  def main(fullConf: FullConf): Unit = {
    val ok = new OkHttpClient
    val clientFactory = () ⇒ new Client(fullConf, ok)

    val testConfs = fullConf.tests

    object ClassTestsCheck extends TestCaseSkeleton(true) {
      override def description = s"Check availability of classes from `${Key.tests}`"
      val stepsF = () ⇒
        for {
          testConf ← testConfs
          className = testConf.className
        } yield {
          TestStep.effect(s"class $className can be instantiated as a ${classOf[TestCase].getSimpleName}")(Class.forName(className).newInstance().asInstanceOf[TestCase])
        }

      def steps = stepsF()
    }

    ClassTestsCheck() match {
      case TestCasePassed ⇒
        // continue ONLY if all test classes can be instantiated
        val testCases =
          for {
            testConf ← testConfs
            className = testConf.className
            theClass = Class.forName(className)
            testCase = theClass.newInstance().asInstanceOf[TestCase]
          } yield {
            testCase → testConf
          }

        runTestCases(clientFactory, testCases)

      case _ ⇒
        sys.exit(5)
    }
  }

  def main(options: Args.GlobalOptions): Unit = {
    val conf = options.conf
    val config = parseConf(conf).resolve()
    //val allHeaders = config.getList(Key.`all-headers`)
    val fullConf = FullConf.parse(config)

    main(fullConf)
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
