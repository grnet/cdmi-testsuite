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

import java.util.UUID

import com.typesafe.config.ConfigRenderOptions
import gr.grnet.cdmi.client.business.{Client, TestCaseSkeleton, TestStep}

/**
 *
 */
class DataObjects extends TestCaseSkeleton(false) {
  val randomFolder   = UUID.randomUUID().toString + "/cdmi/"
  val randomSuffix01 = UUID.randomUUID().toString
  val randomSuffix02 = UUID.randomUUID().toString

  val step01Name = s"PUT CDMI '${Client.Content_Type}: ${Client.Application_Cdmi_Object}'"
  val step01 = TestStep(step01Name) { (client, conf) ⇒

    val objectPath = getObjectPathPrefix(conf) + randomFolder + randomSuffix01
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
    val objectPath = getObjectPathPrefix(conf) + randomFolder + randomSuffix01
    val request = client(objectPath).
      acceptCdmiObject().
      applyHeaders(conf.`http-headers`).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(Client.Application_Cdmi_Object))
  }

  val step01_2_Name = s"GET CDMI '${Client.Accept}: */*' returns '${Client.Content_Type}: ${Client.Application_Cdmi_Object}'"
  val step01_2 = TestStep(step01_2_Name) { (client, conf) ⇒
    val objectPath = getObjectPathPrefix(conf) + randomFolder + randomSuffix01
    val request = client(objectPath).
      acceptAny().
      applyHeaders(conf.`http-headers`).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(Client.Application_Cdmi_Object))
  }

  val step01_3_Name = s"GET CDMI w/o '${Client.Accept}' returns '${Client.Content_Type}: ${Client.Application_Cdmi_Object}'"
  val step01_3 = TestStep(step01_3_Name) { (client, conf) ⇒
    val objectPath = getObjectPathPrefix(conf) + randomFolder + randomSuffix01
    val request = client(objectPath).
      applyHeaders(conf.`http-headers`).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(Client.Application_Cdmi_Object))
  }

  val step02Name = s"PUT non-CDMI '${Client.Content_Type}: ${Client.Text_Plain}'"
  val step02 = TestStep(step02Name) { (client, conf) ⇒
    val objectPath = getObjectPathPrefix(conf) + randomFolder + randomSuffix02
    val text       = getJsonBody(conf).getString("value")
    val mimetype   = getJsonBody(conf).getString("mimetype")

    val request = client(objectPath).
      contentType(mimetype).
      applyHeaders(conf.`http-headers`).
      put(text)

    val response = client.execute(request)
    checkResponse(response, client, false)
  }

  val step02_1_Name = s"GET non-CDMI '${Client.Accept}: */*' returns exact '${Client.Content_Type}'"
  val step02_1 = TestStep(step02_1_Name) { (client, conf) ⇒
    val objectPath = getObjectPathPrefix(conf) + randomFolder + randomSuffix02
    val text       = getJsonBody(conf).getString("value")
    val mimetype   = getJsonBody(conf).getString("mimetype")
    val request = client(objectPath).
      acceptAny().
      applyHeaders(conf.`http-headers`).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(mimetype))
  }

  val step02_2_Name = s"GET non-CDMI w/o '${Client.Accept}' returns exact '${Client.Content_Type}'"
  val step02_2 = TestStep(step02_2_Name) { (client, conf) ⇒
    val objectPath = getObjectPathPrefix(conf) + randomFolder + randomSuffix02
    val text       = getJsonBody(conf).getString("value")
    val mimetype   = getJsonBody(conf).getString("mimetype")
    val request = client(objectPath).
      applyHeaders(conf.`http-headers`).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(mimetype))
  }

  def steps: List[TestStep] = List(
    step01, step01_1, step01_2, step01_3,
    step02, step02_1, step02_2
  )
}
