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

import gr.grnet.cdmi.client.business.{Client, TestStep, TestCaseSkeleton}

/**
 *
 */
class ContainersCDMI extends TestCaseSkeleton(false) {

  val step01Name = s"PUT CDMI '${Client.Content_Type}: ${Client.Application_Cdmi_Container}'"
  val step01 = TestStep(step01Name) { (client, conf) ⇒
    val containerPath = getRandomContainerPath01(conf)

    val request = client(containerPath).
      applyHeaders(conf.`http-headers`).
      contentTypeCdmiContainer().
      put()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(Client.Application_Cdmi_Container))
  }


  def steps: List[TestStep] = List(step01)
}
