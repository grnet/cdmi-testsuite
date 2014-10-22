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

package gr.grnet.cdmi.client.testmodel

import com.squareup.okhttp.{MediaType, Response}
import com.typesafe.config.Config

/**
 *
 */
trait TestCaseSkeleton extends TestCase {
  def checkFailedResponse(response: Response): Unit = {
    val code = response.code()
    val message = response.message()
    assert(!response.isSuccessful, s"!response.isSuccessful [code=$code, msg=$message]")
  }

  def checkResponse(
    response: Response,
    client: HttpClient,
    checkSpecHeader: Boolean,
    checkContentTypeOpt: Option[String] = None
  ): Unit = {
    val code = response.code()
    val message = response.message()
    assert(response.isSuccessful, s"response.isSuccessful [code=$code, msg=$message]")

    if(checkSpecHeader) {
      val specVersion = response.header(HttpClient.X_CDMI_Specification_Version)
      assert(specVersion == client.xCdmiSpecificationVersion, s"specVersion [=$specVersion] == client.xCdmiSpecificationVersion [=${client.xCdmiSpecificationVersion}]")
    }

    checkContentTypeOpt match {
      case None ⇒
      case Some(expectedContentType) ⇒
        val expectedMediaType = MediaType.parse(expectedContentType)
        val expectedTypeSubtype = expectedMediaType.`type`() + "/" + expectedMediaType.subtype()
        val contentType = response.header(HttpClient.Content_Type)
        val mediaType = MediaType.parse(contentType)
        val typeSubtype =  mediaType.`type`() + "/" + mediaType.subtype()
        assert(typeSubtype == expectedTypeSubtype, s"contentType [=$typeSubtype] == expectedContentType [=$expectedTypeSubtype]")
    }
  }

  def checkResponseX(
    response: Response,
    client: HttpClient,
    checkSpecHeader: Boolean,
    checkContentTypeOpt: Option[String] = None
  )(extra: ⇒ Unit): Unit = {
    checkResponse(response, client, checkSpecHeader, checkContentTypeOpt = checkContentTypeOpt)
    extra
  }

  def getObjectPathPrefix(config: TestConfig): String = {
    val TestConfig(global, local) = config
    val path = "object-path-prefix"

    val objectPathPrefix = local.getString(path)

    assert(objectPathPrefix.startsWith("/"), s"objectPathPrefix [=$objectPathPrefix] starts with '/'")
    assert(objectPathPrefix.endsWith("/"), s"objectPathPrefix [=$objectPathPrefix] ends with '/'")

    objectPathPrefix
  }

  def getJsonBody(config: TestConfig): Config = {
    val TestConfig(_, local) = config
    val path = "json-body"
    val jsonBody = local.getConfig(path)
    jsonBody
  }

  def getXAuthToken(config: TestConfig): String = {
    val TestConfig(global, _) = config
    val xAuthToken = global.getString("http-headers.X-Auth-Token")
    assert(!xAuthToken.isEmpty, "!xAuthToken.isEmpty")
    xAuthToken
  }
}

