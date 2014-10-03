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
import gr.grnet.cdmi.client.testmodel.{HttpClient, TestCaseSkeleton, TestStep}

/**
 *
 */
class DataObjects extends TestCaseSkeleton {
  val randomFolder = UUID.randomUUID().toString + "/cdmi/"
  val randomSuffix01 = UUID.randomUUID().toString
  val randomSuffix02 = UUID.randomUUID().toString

  val step01Name = s"PUT CDMI '${HttpClient.Content_Type}: ${HttpClient.Application_Cdmi_Object}'"
  val step01 = TestStep(step01Name) { (config, client) ⇒
    val objectPath = getObjectPathPrefix(config) + randomFolder + randomSuffix01
    val jsonBody   = getJsonBody(config)
    val json       = jsonBody.root().render(ConfigRenderOptions.concise().setFormatted(true))

    val xAuthToken = getXAuthToken(config)
    val request = client(objectPath).
      setXCdmiSpecificationVersion().
      contentTypeCdmiObject().
      xAuthToken(xAuthToken).
      put(json)

    val response = client.execute(request)
    checkResponse(response, client, true)
  }

  val step01_1_Name = s"GET CDMI '${HttpClient.Accept}: ${HttpClient.Application_Cdmi_Object}' returns '${HttpClient.Content_Type}: ${HttpClient.Application_Cdmi_Object}'"
  val step01_1 = TestStep(step01_1_Name) { (config, client) ⇒
    val objectPath = getObjectPathPrefix(config) + randomFolder + randomSuffix01
    val xAuthToken = getXAuthToken(config)
    val request = client(objectPath).
      setXCdmiSpecificationVersion().
      acceptCdmiObject().
      xAuthToken(xAuthToken).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(HttpClient.Application_Cdmi_Object))
  }

  val step01_2_Name = s"GET CDMI '${HttpClient.Accept}: */*' returns '${HttpClient.Content_Type}: ${HttpClient.Application_Cdmi_Object}'"
  val step01_2 = TestStep(step01_2_Name) { (config, client) ⇒
    val objectPath = getObjectPathPrefix(config) + randomFolder + randomSuffix01
    val xAuthToken = getXAuthToken(config)
    val request = client(objectPath).
      setXCdmiSpecificationVersion().
      acceptAny().
      xAuthToken(xAuthToken).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(HttpClient.Application_Cdmi_Object))
  }

  val step01_3_Name = s"GET CDMI w/o '${HttpClient.Accept}' returns '${HttpClient.Content_Type}: ${HttpClient.Application_Cdmi_Object}'"
  val step01_3 = TestStep(step01_3_Name) { (config, client) ⇒
    val objectPath = getObjectPathPrefix(config) + randomFolder + randomSuffix01
    val xAuthToken = getXAuthToken(config)
    val request = client(objectPath).
      setXCdmiSpecificationVersion().
      xAuthToken(xAuthToken).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(HttpClient.Application_Cdmi_Object))
  }

  val step01_4_Name = s"GET CDMI w/o '${HttpClient.X_Auth_Token}' fails"
  val step01_4 = TestStep(step01_4_Name) { (config, client) ⇒
    val objectPath = getObjectPathPrefix(config) + randomFolder + randomSuffix01
    val request = client(objectPath).
      setXCdmiSpecificationVersion().
      get()

    val response = client.execute(request)
    checkFailedResponse(response)
  }

  val step02Name = s"PUT non-CDMI '${HttpClient.Content_Type}: ${HttpClient.Text_Plain}'"
  val step02 = TestStep(step02Name) { (config, client) ⇒
    val objectPath = getObjectPathPrefix(config) + randomFolder + randomSuffix02
    val text       = getJsonBody(config).getString("value")
    val mimetype   = getJsonBody(config).getString("mimetype")
    val xAuthToken = getXAuthToken(config)

    val request = client(objectPath).
      contentType(mimetype).
      xAuthToken(xAuthToken).
      put(text)

    val response = client.execute(request)
    checkResponse(response, client, false)
  }

  val step02_1_Name = s"GET non-CDMI '${HttpClient.Accept}: */*' returns exact '${HttpClient.Content_Type}'"
  val step02_1 = TestStep(step02_1_Name) { (config, client) ⇒
    val objectPath = getObjectPathPrefix(config) + randomFolder + randomSuffix02
    val text       = getJsonBody(config).getString("value")
    val mimetype   = getJsonBody(config).getString("mimetype")
    val xAuthToken = getXAuthToken(config)
    val request = client(objectPath).
      acceptAny().
      xAuthToken(xAuthToken).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(mimetype))
  }

  val step02_2_Name = s"GET non-CDMI w/o '${HttpClient.Accept}' returns exact '${HttpClient.Content_Type}'"
  val step02_2 = TestStep(step02_2_Name) { (config, client) ⇒
    val objectPath = getObjectPathPrefix(config) + randomFolder + randomSuffix02
    val text       = getJsonBody(config).getString("value")
    val mimetype   = getJsonBody(config).getString("mimetype")
    val xAuthToken = getXAuthToken(config)
    val request = client(objectPath).
      xAuthToken(xAuthToken).
      get()

    val response = client.execute(request)
    checkResponse(response, client, true, Some(mimetype))
  }

  def steps: List[TestStep] = List(
    step01, step01_1, step01_2, step01_3, step01_4,
    step02, step02_1, step02_2
  )
}
