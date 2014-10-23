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

package gr.grnet.cdmi.client.conf

import java.net.URI

import com.typesafe.config.Config
import scala.collection.JavaConverters._
import MasterConf.HttpHeaders

/**
 *
 */
case class ProfileConf(
  name: String,
  rootURI: URI,
  httpHeaders: HttpHeaders,
  classTestsList: List[String]/*,
  classTests: List[ClassTestConf]*/
) {
  def overrideWith(that: ProfileConf): ProfileConf =
    ProfileConf(
      name = that.name,
      rootURI = if(that.rootURI.toString.isEmpty) this.rootURI else that.rootURI,
      httpHeaders = if(that.httpHeaders.isEmpty) this.httpHeaders else that.httpHeaders,
      classTestsList = if(that.classTestsList.isEmpty) this.classTestsList else that.classTestsList
    )
}

case class ClassTestConf(
  classTestName: String,
  httpHeaders: HttpHeaders,
  specific: Config // e.g. when you want to define an HTTP payload
)

case class MasterConf(
  profiles: List[ProfileConf],
  classTests: List[ClassTestConf],
  classTestsList: List[String]
)

object MasterConf {
  type HttpHeaders = Map[String, String]
  type ConfError = Either[String, Exception] // both are bad here

  object ConfError {
    def ofString   (s: String)   : ConfError = Left (s)
    def ofException(e: Exception): ConfError = Right(e)
  }

  def parseHttpHeaders(c: Config): HttpHeaders = {
    val httpHeadersSeq =
      for {
        entry ← c.getObject(ConfKey.`http-headers`).entrySet().asScala.toSeq
        key = entry.getKey
        cvalue = entry.getValue if cvalue.isInstanceOf[String]
        value = s"$cvalue"
      }
      yield key → value

    val httpHeaders = Map(httpHeadersSeq:_*)

    httpHeaders
  }

  def parseStringList(c: Config, key: String): List[String] = c.getStringList(key).asScala.toList

  def parseClassTest(
    classTestName: String,
    classTestConfig: Config,
    profileConf: ProfileConf,
    xConfigProfileConf: ProfileConf // will be used to override `profileConf`
  ): ClassTestConf = {
    val specific = classTestConfig.getConfig(ConfKey.specific)
    val httpHeaders =
      if(xConfigProfileConf.httpHeaders.isEmpty)
        profileConf.httpHeaders
      else
        xConfigProfileConf.httpHeaders

    ClassTestConf(classTestName, httpHeaders, specific)
  }

  def parseProfile(name: String, profileConfig: Config): ProfileConf = {
    val rootURI = new URI(profileConfig.getString(ConfKey.`root-uri`))
    val httpHeaders = parseHttpHeaders(profileConfig)
    val classTestsList = parseStringList(profileConfig, ConfKey.`class-tests-list`)

    ProfileConf(name, rootURI, httpHeaders, classTestsList)
  }

  def parseProfiles(c: Config): List[ProfileConf] = {
    val profilesConfig = c.getConfig(ConfKey.profiles)
    val profileNames = profilesConfig.entrySet().asScala.map(_.getKey)

    val profiles =
      for {
        name ← profileNames
        profileConfig ← profilesConfig.getConfig(name)
      } yield parseProfile(name, profileConfig)

    profiles
  }



  // The schema of `xconfig` must match the schema of `config`
  def parse(config: Config, xConfig: Config, profileName: String): Either[ConfError, MasterConf] = {
    val profiles = parseProfiles(config)
    val profileOpt = profiles.find(_.name == profileName)
    val xProfiles = parseProfiles(xConfig)
    val xProfileOpt = xProfiles.find(_.name == profileName)

    val profile = (profileOpt, xProfileOpt) match {
      case (Some(profile), None) ⇒
        // No override, use the original
        profile

      case (Some(profile), Some(xProfile)) ⇒
        xProfile.
    }
    xProfileOpt match {
      case None ⇒
        Left(ConfError.ofString(s"Profile $profileName does not e"))
    }

    val httpHeaders = parseHttpHeaders(config)
    val classTestsList = parseStringList(config, ConfKey.`class-tests-list`)


    null
  }
}
