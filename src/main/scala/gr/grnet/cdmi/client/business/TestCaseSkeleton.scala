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

package gr.grnet.cdmi.client.business

import com.squareup.okhttp.{MediaType, Response}
import com.typesafe.config.Config
import gr.grnet.cdmi.client.conf.TestConf

/**
 *
 */
abstract class TestCaseSkeleton(val fatalOnError: Boolean) extends TestCase {
  def checkNot500(response: Response): Unit = {
    val code = response.code()
    val message = response.message()
    val body = response.body().string()
    val is500 = code >= 500 && code < 600

    assert(!is500, s"!is500 [$code|$message|$body]")
  }

  def checkFailedResponse(response: Response): Unit = {
    val code = response.code()
    val message = response.message()
    val body = response.body().string()
    assert(!response.isSuccessful, s"!response.isSuccessful [$code|$message|$body]")
  }

  def checkResponse(
    response: Response,
    client: Client,
    checkSpecHeader: Boolean,
    checkContentTypeOpt: Option[String] = None
  ): Unit = {
    val code = response.code()
    val message = response.message()
    val body = response.body().string()
    assert(response.isSuccessful, s"response.isSuccessful [$code|$message|$body]")

    if(checkSpecHeader) {
      val specVersion = response.header(Client.X_CDMI_Specification_Version)

      assert(specVersion == client.fullConf.`cdmi-spec-version`, s"specVersion [=$specVersion] == client.xCdmiSpecificationVersion [=${client.xCdmiSpecificationVersion}]")
    }

    checkContentTypeOpt match {
      case None ⇒
      case Some(expectedContentType) ⇒
        val expectedMediaType = MediaType.parse(expectedContentType)
        val expectedTypeSubtype = expectedMediaType.`type`() + "/" + expectedMediaType.subtype()
        val contentType = response.header(Client.Content_Type)
        val mediaType = MediaType.parse(contentType)
        val typeSubtype =  mediaType.`type`() + "/" + mediaType.subtype()
        assert(typeSubtype == expectedTypeSubtype, s"contentType [=$typeSubtype] == expectedContentType [=$expectedTypeSubtype]")
    }
  }

  def checkResponseX(
    response: Response,
    client: Client,
    checkSpecHeader: Boolean,
    checkContentTypeOpt: Option[String] = None
  )(extra: ⇒ Unit): Unit = {
    checkResponse(response, client, checkSpecHeader, checkContentTypeOpt = checkContentTypeOpt)
    extra
  }

  def getObjectPathPrefix(conf: TestConf): String = {
    val path = "object-path-prefix"
    val specific = conf.specific

    val objectPathPrefix = specific.getString(path)

    assert(objectPathPrefix.startsWith("/"), s"objectPathPrefix [=$objectPathPrefix] starts with '/'")
    assert(objectPathPrefix.endsWith("/"), s"objectPathPrefix [=$objectPathPrefix] ends with '/'")

    objectPathPrefix
  }

  def getJsonBody(conf: TestConf): Config = {
    val path = "json-body"
    val specific = conf.specific
    val jsonBody = specific.getConfig(path)
    jsonBody
  }
}

