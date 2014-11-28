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

import com.typesafe.config.ConfigRenderOptions
import gr.grnet.cdmi.client.business.{Client, TestCaseSkeleton, TestStep}

/**
 *
 */
class DataObjectsCDMI extends TestCaseSkeleton(false) {
  val step01Name = s"PUT CDMI '${Client.Content_Type}: ${Client.Application_Cdmi_Object}'"
  val step01 = TestStep(step01Name) { (client, conf) ⇒

    val objectPath = getRandomTestObjectPath01(conf)
    val jsonBody   = getJsonBody(conf)
    val json       = jsonBody.root().render(ConfigRenderOptions.concise().setFormatted(true))

    val request = client(objectPath).
      contentTypeCdmiObject().
      applyHeaders(conf.`http-headers`).
      put(json)

    val response = client.execute(request)
    checkResponse(response, client, true)
  }

  val step01_1_Name = s"GET CDMI '${Client.Accept}: ${Client.Application_Cdmi_Object}' returns '${Client.Content_Type}: ${Client.Application_Cdmi_Object}'"
  val step01_1 = TestStep(step01_1_Name) { (client, conf) ⇒
    val objectPath = getRandomTestObjectPath01(conf)
    val request = client(objectPath).
      acceptCdmiObject().
      applyHeaders(conf.`http-headers`).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(Client.Application_Cdmi_Object))
  }

  val step01_2_Name = s"GET CDMI '${Client.Accept}: */*' returns '${Client.Content_Type}: ${Client.Application_Cdmi_Object}'"
  val step01_2 = TestStep(step01_2_Name) { (client, conf) ⇒
    val objectPath = getRandomTestObjectPath01(conf)
    val request = client(objectPath).
      acceptAny().
      applyHeaders(conf.`http-headers`).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(Client.Application_Cdmi_Object))
  }

  val step01_3_Name = s"GET CDMI w/o '${Client.Accept}' returns '${Client.Content_Type}: ${Client.Application_Cdmi_Object}'"
  val step01_3 = TestStep(step01_3_Name) { (client, conf) ⇒
    val objectPath = getRandomTestObjectPath01(conf)
    val request = client(objectPath).
      applyHeaders(conf.`http-headers`).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(Client.Application_Cdmi_Object))
  }

  def steps: List[TestStep] = List(step01, step01_1, step01_2, step01_3)
}
