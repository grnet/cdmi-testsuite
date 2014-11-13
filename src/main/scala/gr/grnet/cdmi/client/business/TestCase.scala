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
package business

import gr.grnet.cdmi.client.conf.TestConf

import scala.annotation.tailrec

trait TestCase {
  // If this is true, then subsequent test cases are not tried
  def fatalOnError: Boolean

  def steps: List[TestStep]

  def id = getClass.getName

  def description = {
    val name = this.getClass.getName
    val i = name.lastIndexOf('.')
    name.substring(i + 1)
  }

  def apply(): TestCaseResult = apply(null, null)

  def apply(client: Client, conf: TestConf): TestCaseResult = {
    def LOG(s: String): Unit = System.out.println(s)

    def applyStep(step: TestStep): Option[Throwable] =
      try {
        step(client, conf)
        None
      }
      catch {
        case e: Exception ⇒ Some(e)
        case e: AssertionError ⇒ Some(e)
      }

    @tailrec
    def loopSteps(steps: List[TestStep]): TestCaseResult =
      steps match {
        case step :: moreSteps ⇒
          applyStep(step) match {
            case None ⇒
              // Good, go on
              LOG(s"  OK [${step.description}]")
              loopSteps(moreSteps)

            case someException @ Some(exception) ⇒
              // End prematurely
              LOG(s"  KO [${step.description}] (⇒ ${exception.getMessage})")
              TestCaseNotPassed(step.description, someException)
          }

        case Nil ⇒
          // Everything has run and we are OK
          TestCasePassed
      }

    val simpleClassName = this.getClass.getName
    LOG(s"+$description [$simpleClassName]")

    try {
      val testCaseResult = loopSteps(steps)

      testCaseResult match {
        case TestCasePassed ⇒
          LOG(s"-OK $description [$simpleClassName]")
        case TestCaseNotPassed(_, _) ⇒
          LOG(s"-KO $description [$simpleClassName]")
      }

      testCaseResult
    }
    catch {
      case e: Throwable ⇒
        val msg = "<testcase internal error>"
        LOG(s"     $msg")
        val sst = e.stringStackTrace("     ")
        LOG(sst)
        LOG(s"-KO $description [$simpleClassName]")
        TestCaseNotPassed(msg, Some(e))
    }
  }
}
