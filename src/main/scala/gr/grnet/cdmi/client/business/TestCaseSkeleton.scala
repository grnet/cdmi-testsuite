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

import java.util.UUID

import com.squareup.okhttp.{MediaType, Response}
import com.typesafe.config.{Config, ConfigFactory}
import gr.grnet.cdmi.client.conf.TestConf

/**
 *
 */
abstract class TestCaseSkeleton(val fatalOnError: Boolean) extends TestCase {
  val randomFolder = UUID.randomUUID().toString + "/cdmi/"
  val randomObjectSuffix01 = UUID.randomUUID().toString
  val randomObjectSuffix02 = UUID.randomUUID().toString
  val randomContainerSuffix01 = UUID.randomUUID().toString + "/"
  val randomContainerSuffix02 = UUID.randomUUID().toString + "/" + UUID.randomUUID().toString + "/" // nested container
  val randomContainerSuffix03 = UUID.randomUUID().toString + "/"

  def assertJsonPath(config: Config, path: String): Unit =
    assert(config.hasPath(path), s"'$path' exists in the returned JSON")

  def assertJsonPaths(config: Config, paths: String*): Unit =
    for(path ← paths) assertJsonPath(config, path)

  def assertJsonPathAndValue(config: Config, path: String, value: String): Unit = {
    assertJsonPath(config, path)
    val foundValue = config.getString(path)
    assert(foundValue == value, s"'$path: $foundValue' instead of '$value' in the returned JSON")
  }

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
  ): String = {
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

    body
  }

  def checkResponseX(
    response: Response,
    client: Client,
    checkSpecHeader: Boolean,
    checkContentTypeOpt: Option[String] = None
  )(extraBodyCheck: (String) ⇒ Unit): Unit = {
    val body = checkResponse(response, client, checkSpecHeader, checkContentTypeOpt = checkContentTypeOpt)
    extraBodyCheck(body)
  }

  def checkCdmiContainerResponseFields(bodyString: String): Unit = {
    val bodyConfig = ConfigFactory.parseString(bodyString)

    // Check mandatory fields in the JSON response
    // CDMI/1.0.2 Section 9.2.7 Table 35
    assertJsonPathAndValue(bodyConfig, "objectType", Client.Application_Cdmi_Container)
    assertJsonPaths(bodyConfig,
      "objectID",
      "objectName",
      "parentURI",
      "parentID",
      "domainURI",
      "capabilitiesURI",
      "completionStatus",
      "metadata",
      "childrenrange",
      "children"
    )
  }

  def getObjectPathPrefix(conf: TestConf): String = {
    val specific = conf.specific
    val path = "object-path-prefix"
    val objectPathPrefix = specific.getString(path)

    assert(objectPathPrefix.startsWith("/"), s"objectPathPrefix [=$objectPathPrefix] starts with '/'")
    assert(objectPathPrefix.endsWith("/")  , s"objectPathPrefix [=$objectPathPrefix] ends with '/'"  )

    objectPathPrefix
  }

  def getRandomTestObjectPath01(conf: TestConf): String = getObjectPathPrefix(conf) + randomFolder + randomObjectSuffix01
  def getRandomTestObjectPath02(conf: TestConf): String = getObjectPathPrefix(conf) + randomFolder + randomObjectSuffix02

  def getContainerPathPrefix(conf: TestConf): String = {
    val specific = conf.specific
    val path = "container-path-prefix"
    val containerPathPrefix = specific.getString(path)

    assert(containerPathPrefix.startsWith("/"), s"containerPathPrefix [=$containerPathPrefix] starts with '/'")
    assert(containerPathPrefix.endsWith("/")  , s"containerPathPrefix [=$containerPathPrefix] ends with '/'"  )

    containerPathPrefix
  }

  def getRandomContainerPath01(conf: TestConf): String = getContainerPathPrefix(conf) + randomFolder + randomContainerSuffix01
  def getRandomContainerPath02(conf: TestConf): String = getContainerPathPrefix(conf) + randomFolder + randomContainerSuffix02
  def getRandomContainerPath03(conf: TestConf): String = getContainerPathPrefix(conf) + randomFolder + randomContainerSuffix03

  def getJsonBody(conf: TestConf): Config = {
    val path = "json-body"
    val specific = conf.specific
    val jsonBody = specific.getConfig(path)
    jsonBody
  }
}

