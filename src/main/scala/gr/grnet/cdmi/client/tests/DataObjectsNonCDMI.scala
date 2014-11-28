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

/**
 *
 */
class DataObjectsNonCDMI extends TestCaseSkeleton(false) {
  val step01Name = s"PUT non-CDMI '${Client.Content_Type}: ${Client.Text_Plain}'"
  val step01 = TestStep(step01Name) { (client, conf) ⇒
    val objectPath = getRandomTestObjectPath02(conf)
    val text       = getJsonBody(conf).getString("value")
    val mimetype   = getJsonBody(conf).getString("mimetype")

    val headers = conf.`http-headers`
    val request = client(objectPath).
      applyHeaders(headers).
      clearHeader(Client.X_CDMI_Specification_Version). // spec version must not be present for non-CDMI call
      contentType(mimetype).
      put(text)

    val response = client.execute(request)
    checkResponse(response, client, false)
  }

  val step01_1_Name = s"GET non-CDMI '${Client.Accept}: */*' returns exact '${Client.Content_Type}'"
  val step01_1 = TestStep(step01_1_Name) { (client, conf) ⇒
    val objectPath = getRandomTestObjectPath02(conf)
    val text       = getJsonBody(conf).getString("value")
    val mimetype   = getJsonBody(conf).getString("mimetype")
    val request = client(objectPath).
      applyHeaders(conf.`http-headers`).
      clearHeader(Client.X_CDMI_Specification_Version). // spec version must not be present for non-CDMI call
      acceptAny().
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(mimetype))
  }

  val step01_2_Name = s"GET non-CDMI w/o '${Client.Accept}' returns exact '${Client.Content_Type}'"
  val step01_2 = TestStep(step01_2_Name) { (client, conf) ⇒
    val objectPath = getRandomTestObjectPath02(conf)
    val text       = getJsonBody(conf).getString("value")
    val mimetype   = getJsonBody(conf).getString("mimetype")
    val request = client(objectPath).
      applyHeaders(conf.`http-headers`).
      clearHeader(Client.X_CDMI_Specification_Version). // spec version must not be present for non-CDMI call
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(mimetype))
  }

  val step01_3_Name = s"DELETE non-CDMI"
  val step01_3 = TestStep(step01_3_Name) { (client, conf) ⇒
    val objectPath = getRandomTestObjectPath02(conf)
    val request = client(objectPath).
      applyHeaders(conf.`http-headers`).
      clearHeader(Client.X_CDMI_Specification_Version). // spec version must not be present for non-CDMI call
      delete()

    val response = client.execute(request)
    checkResponse(response, client, true, None)
  }

  def steps: List[TestStep] = List(step01, step01_1, step01_2, step01_3)
}
