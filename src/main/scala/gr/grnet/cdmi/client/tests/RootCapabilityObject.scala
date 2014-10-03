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

import com.squareup.okhttp.{Request, Response}
import com.typesafe.config.ConfigFactory
import gr.grnet.cdmi.Capabilities
import gr.grnet.cdmi.client.testmodel.{HttpClient, TestCaseSkeleton, TestStep}

import scala.collection.JavaConverters._

/**
 * 
 */
class RootCapabilityObject extends TestCaseSkeleton {
  def checkCapabilities(response: Response): Unit = {
    val bodyString = response.body().string()
    val bodyConfig = ConfigFactory.parseString(bodyString)
    assert(bodyConfig.hasPath("capabilities"), "'capabilities' field exists in the returned JSON")

    val capabilitiesConfig = bodyConfig.getConfig("capabilities")
    val capabilities = capabilitiesConfig.entrySet().asScala.map(_.getKey)

    for {
      capability ← capabilities
    } {
      assert(Capabilities.SystemWideNameSet(capability), s"'$capability' is a known system wide capability")
    }
  }

  def allChecks(request: Request, client: HttpClient) {
    val response = client.execute(request)
    checkResponse(response, client, true, Some(HttpClient.Application_Cdmi_Capability))
    checkCapabilities(response)
  }


  val step01Name = s"Get capabilities with '${HttpClient.X_CDMI_Specification_Version}', no 'Accept'"
  val step01 = TestStep(step01Name) { (config, client) ⇒
    val request = client("/cdmi_capabilities/").
      setXCdmiSpecificationVersion().
      get()

    allChecks(request, client)
  }

  val step02Name = s"Get capabilities with '${HttpClient.X_CDMI_Specification_Version}' and 'Accept: ${HttpClient.Application_Cdmi_Capability}'"
  val step02 = TestStep(step02Name) { (config, client) ⇒
    val request = client("/cdmi_capabilities/").
      setXCdmiSpecificationVersion(). // 12.2.3 mandatory
      acceptCdmiCapability().
      get()

    allChecks(request, client)
  }

  val step03Name = s"Get capabilities with '${HttpClient.X_CDMI_Specification_Version}' and 'Accept: */*'"
  val step03 = TestStep(step03Name) { (config, client) ⇒
    val request = client("/cdmi_capabilities/").
      setXCdmiSpecificationVersion(). // 12.2.3 mandatory
      acceptAny().
      get()

    allChecks(request, client)
  }

  val step04Name = s"Get capabilities w/o '${HttpClient.X_CDMI_Specification_Version}'"
  val step04 = TestStep(step04Name) { (config, client) ⇒
    val request = client.
      apply("/cdmi_capabilities/").
      get()

    val response = client.execute(request)
    checkResponse(response, client, false, Some(HttpClient.Application_Cdmi_Capability))
  }

  def steps: List[TestStep] = List(step01, step02, step03, step04)
}
