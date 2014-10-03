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

import com.squareup.okhttp._
import com.typesafe.config.Config

class HttpClient(
  val rootUri: String,
  ok: OkHttpClient,
  extraHeaders: Config
) {

  import HttpClient._

  def xCdmiSpecificationVersion = extraHeaders.getString(HttpClient.X_CDMI_Specification_Version)

  class HttpRequestBuilder(okBuilder: Request.Builder) {
    var _contentType: String = _
    def header(name: String, value: String): HttpRequestBuilder = {
      okBuilder.addHeader(name, value)
      if(name == HttpClient.Content_Type) _contentType = value
      this
    }

    def fromExtraHeaders(name: String): HttpRequestBuilder = {
      val value = extraHeaders.getString(name)
      header(name, value)
    }

    def setXCdmiSpecificationVersion(version: String): HttpRequestBuilder =
      header(X_CDMI_Specification_Version, version)

    def setXCdmiSpecificationVersion(): HttpRequestBuilder =
      setXCdmiSpecificationVersion(xCdmiSpecificationVersion)

    def accept(mime: String)   = header(Accept, mime)
    def acceptAny()            = header(Accept, "*/*")
    def acceptCdmiCapability() = accept(Application_Cdmi_Capability)
    def acceptCdmiContainer()  = accept(Application_Cdmi_Container)
    def acceptCdmiObject()     = accept(Application_Cdmi_Object)

    def contentType(mime: String) = header(Content_Type, mime)
    def contentTypeCdmiObject()   = contentType(Application_Cdmi_Object)

    def xAuthToken(token: String) = header(HttpClient.X_Auth_Token, token)

    def get   (): Request                  = okBuilder.get().build()
    def put   (body: RequestBody): Request = okBuilder.put(body).build()
    def post  (body: RequestBody): Request = okBuilder.post(body).build()
    def delete(): Request                  = okBuilder.delete().build()

    def put (mediaType: String, body: String): Request = put(RequestBody.create(MediaType.parse(mediaType), body))
    def post(mediaType: String, body: String): Request = post(RequestBody.create(MediaType.parse(mediaType), body))

    def put(body: String): Request =
      _contentType match {
        case null ⇒
          throw new Exception(s"No ${HttpClient.Content_Type} given for PUT")

        case _ ⇒
          put(_contentType, body)
      }

    def post(body: String): Request =
      _contentType match {
        case null ⇒
          throw new Exception(s"No ${HttpClient.Content_Type} given for POST")

        case _ ⇒
          post(_contentType, body)
      }
  }

  def makeUrl(path: String): String =
    (rootUri.endsWith("/"), path.startsWith("/")) match {
      case (true, true)   ⇒ rootUri + path.substring(1)
      case (true, false)  ⇒ rootUri + path
      case (false, true)  ⇒ rootUri + path
      case (false, false) ⇒ rootUri + "/" + path
    }

  def apply(path: String): HttpRequestBuilder = {
    val okBuilder = new Request.Builder().url(makeUrl(path))
    new HttpRequestBuilder(okBuilder)
  }

  def get(path: String) = apply(path).get()

  def put(path: String, body: RequestBody) = apply(path).put(body)

  def post(path: String, body: RequestBody) = apply(path).post(body)

  def delete(path: String) = apply(path).delete()

  def execute(request: Request): Response = ok.newCall(request).execute()
}

object HttpClient {
  val X_CDMI_Specification_Version = "X-CDMI-Specification-Version"
  val X_Auth_Token = "X-Auth-Token"
  val Accept = "Accept"
  val Content_Type = "Content-Type"

  val Application_Cdmi_Capability = "application/cdmi-capability"
  val Application_Cdmi_Container   = "application/cdmi-container"
  val Application_Cdmi_Object      = "application/cdmi-object"

  val Text_Plain = "text/plain"
}