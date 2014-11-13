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

package gr.grnet.cdmi.client.tests

import gr.grnet.cdmi.client.business.{TestCaseSkeleton, TestStep}

/**
 *
 */
class ServerConnection extends TestCaseSkeleton {
  val step01 = TestStep("Ping Server") { (client, conf) ⇒
    val request = client("/cdmi_capabilities/").get()
    val _ = client.execute(request)
  }

  def steps: List[TestStep] = List(step01)
}
