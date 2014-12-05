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

import gr.grnet.cdmi.client.business.{Client, TestCaseSkeleton, TestStep}

/** Test nested containers */
class ContainersCDMI_IV extends TestCaseSkeleton(false) {

  val step01Name = s"PUT CDMI w/ '${Client.Content_Type}: ${Client.Application_Cdmi_Container}' and '${Client.Accept}: ${Client.Application_Cdmi_Container}'"
  val step01 = TestStep(step01Name) { (client, conf) ⇒
    val containerPath = getRandomContainerPath04(conf)

    val request = client(containerPath).
      applyHeaders(conf.`http-headers`).
      contentTypeCdmiContainer().
      acceptCdmiContainer().
      put()

    val response = client.execute(request)

    checkResponseX(response, client, true, Some(Client.Application_Cdmi_Container)) { bodyString ⇒
      checkCdmiContainerResponseFields(bodyString)
    }
  }

  val step02_1_Name = s"GET CDMI w/ '${Client.Accept}: ${Client.Application_Cdmi_Container}'"
  val step02_1 = TestStep(step02_1_Name) { (client, conf) ⇒
    val containerPath = getRandomContainerPath04(conf)

    val request = client(containerPath).
      applyHeaders(conf.`http-headers`).
      acceptCdmiContainer().
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(Client.Application_Cdmi_Container))
  }

  val step02_2_Name = s"GET CDMI w/o '${Client.Accept}'"
  val step02_2 = TestStep(step02_2_Name) { (client, conf) ⇒
    val containerPath = getRandomContainerPath04(conf)

    val request = client(containerPath).
      applyHeaders(conf.`http-headers`).
      clearHeader(Client.Accept).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(Client.Application_Cdmi_Container))
  }

  val step02_3_Name = s"GET CDMI w/ '${Client.Accept}: */*'"
  val step02_3 = TestStep(step02_3_Name) { (client, conf) ⇒
    val containerPath = getRandomContainerPath04(conf)

    val request = client(containerPath).
      applyHeaders(conf.`http-headers`).
      acceptAny().
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(Client.Application_Cdmi_Container))
  }

  val step03Name = s"DELETE CDMI"
  val step03 = TestStep(step03Name) { (client, conf) ⇒
    val containerPath = getRandomContainerPath04(conf)

    val request = client(containerPath).
      applyHeaders(conf.`http-headers`).
      delete()

    val response = client.execute(request)
    checkResponse(response, client, true)
  }

  def steps: List[TestStep] = List(step01, step02_1, step02_2, step02_3, step03)
}
