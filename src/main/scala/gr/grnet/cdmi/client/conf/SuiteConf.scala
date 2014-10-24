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

import com.typesafe.config.{Config, ConfigFactory}
import gr.grnet.cdmi.client.conf.SuiteConf.HttpHeaders

import scala.collection.JavaConverters._

/** Parsed profile configuration */
case class ProfileConf(
  name: String,
  rootURI: URI,
  httpHeaders: HttpHeaders,
  classTestNames: List[String],
  classTestsHeaderNames: Map[String, List[String]],
  effectiveClassTests: List[ClassTestConf] = Nil,     // this is filled in after some post-processing
  effectiveClassTestsHeaders: Map[String, Map[String, String]] = Map() // this is filled in after some post-processing
) {

  def overrideWith(that: ProfileConf): ProfileConf =
    ProfileConf(
      name = that.name,
      rootURI = if(that.rootURI.toString.isEmpty) this.rootURI else that.rootURI,
      httpHeaders = if(that.httpHeaders.isEmpty) this.httpHeaders else that.httpHeaders,
      classTestNames = if(that.classTestNames.isEmpty) this.classTestNames else that.classTestNames,
      classTestsHeaderNames = if(that.classTestsHeaderNames.isEmpty) this.classTestsHeaderNames else that.classTestsHeaderNames,
      effectiveClassTests = that.effectiveClassTests,
      effectiveClassTestsHeaders = that.effectiveClassTestsHeaders
    )
}

object ProfileConf {
  val Empty = ProfileConf("", new URI(""), Map(), Nil, Map(), Nil, Map())
}

/** Parsed configuration of a test (defined in a class) */
case class ClassTestConf(
  testClassName: String,
  specific: Config // e.g. when you want to define an HTTP payload
)
object ClassTestConf {
  val Empty = ClassTestConf("", ConfigFactory.empty())
}

case class SuiteConf(
  profiles: List[ProfileConf],
  classTests: List[ClassTestConf],
  classTestsList: List[String]
)

object SuiteConf {
  type HttpHeaders = Map[String, String]
  type ConfError = String

  object ConfError { def apply(s: String): ConfError = s }

  private def parseHttpHeaders(c: Config): HttpHeaders = {
    if(!c.hasPath(ConfKey.`http-headers`)) {
      return Map()
    }

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

  private def parseStringList(c: Config, key: String): List[String] =
    if(c.hasPath(key)) {
      c.getStringList(key).asScala.toList
    }
    else {
      Nil
    }

  private def parseClassTest(testClassName: String, classTestConfig: Config): ClassTestConf = {
    val key = ConfKey.specific
    val specific =
      if(classTestConfig.hasPath(key))
        classTestConfig.getConfig(key)
      else
        ConfigFactory.empty()

    ClassTestConf(testClassName, specific)
  }

  private def parseClassTests(c: Config): List[ClassTestConf] = {
    val key = ConfKey.`class-tests`
    if(!c.hasPath(key)) {
      return Nil
    }

    val classTestsConfig = c.getConfig(key)
    val testClassNames = classTestsConfig.entrySet().asScala.map(_.getKey).toList

    for {
      testClassName ← testClassNames
      classTestConfig = c.getConfig(testClassName)
    } yield parseClassTest(testClassName, classTestConfig)
  }

  private def parseURI(c: Config, key: String): URI =
    if(c.hasPath(key))
      new URI(c.getString(key))
    else
      new URI("")

  private def parseClassTestsHeaderLists(profileConfig: Config): Map[String, List[String]] = {
    val key = ConfKey.`class-tests-header-lists`
    val classTestsHeaderListsConfig =
      if(profileConfig.hasPath(key)) {
        profileConfig.getConfig(key)
      }
      else {
        ConfigFactory.empty()
      }

    val testClassNames = classTestsHeaderListsConfig.entrySet().asScala.map(_.getKey).toList

    def loop(classNames: List[String], mapAccum: Map[String, List[String]]): Map[String, List[String]] =
      classNames match {
        case Nil ⇒
          mapAccum

        case className :: remaining ⇒
          val headersNames = parseStringList(classTestsHeaderListsConfig, className)
          val newMapAccum = mapAccum + (className → headersNames)

          loop(remaining, newMapAccum)
      }

    loop(testClassNames, Map())
  }

  private def parseProfile(name: String, profileConfig: Config): ProfileConf = {
    val rootURI = parseURI(profileConfig, ConfKey.`root-uri`)
    val httpHeaders = parseHttpHeaders(profileConfig)
    val classTestsList = parseStringList(profileConfig, ConfKey.`class-tests-list`)
    val classTestsHeaderLists = parseClassTestsHeaderLists(profileConfig)

    ProfileConf(name, rootURI, httpHeaders, classTestsList, classTestsHeaderLists)
  }

  private def parseProfiles(c: Config): List[ProfileConf] = {
    val profilesConfig = c.getConfig(ConfKey.profiles)
    val profileNames = profilesConfig.entrySet().asScala.map(_.getKey)

    val profiles =
      for {
        name ← profileNames
        profileConfig ← profilesConfig.getConfig(name)
      } yield parseProfile(name, profileConfig)

    profiles
  }

  // The schema of `xconfig` must match the schema of `cConfig`
  def parse(cConfig: Config, p: String, xConfig: Config): Either[ConfError, ProfileConf] = {

    ///////////////////////////////
    // 1. Profile
    val cProfiles = parseProfiles(cConfig)
    val xProfiles = parseProfiles(xConfig)
    val cProfileOpt = cProfiles.find(_.name == p)
    val xProfileOpt = xProfiles.find(_.name == p)

    lazy val noProfileErrMsg = s"Profile $p does not exist in configuration"
    lazy val noProfileErr = Left[ConfError, ProfileConf](ConfError(noProfileErrMsg))
    if(cProfileOpt.isEmpty && xProfileOpt.isEmpty) {
      return noProfileErr
    }

    val profile = (cProfileOpt, xProfileOpt) match {
      case (Some(cProfile), None) ⇒
        // Use the original if there is no `xProfile`
        cProfile

      case (Some(cProfile), Some(xProfile)) ⇒
        // `xProfile` overrides `cProfile`
        cProfile overrideWith xProfile

      case (None, Some(xProfile)) ⇒
        xProfile

      case _ ⇒
        return noProfileErr
    }

    ///////////////////////////////
    // 2. `class-tests-list`
    val cClassTestNames = parseStringList(cConfig, ConfKey.`class-tests-list`)
    val effectiveClassTestNames =
      if(profile.classTestNames.isEmpty)
        cClassTestNames
      else
        profile.classTestNames

    ///////////////////////////////
    // 3. ClassTest
    val classTests = parseClassTests(cConfig)
    val classTestsMap = classTests.map(ct ⇒ (ct.testClassName, ct)).toMap
    val effectiveClassTests =
      for {
        effectiveClassTestName ← effectiveClassTestNames
        effectiveClassTest = classTestsMap(effectiveClassTestName)
      } yield effectiveClassTest

    // TODO Take into account those headers that are specified in ProfileConf.classTestsHeaderNames


    val effectiveProfile = profile.copy(effectiveClassTests = effectiveClassTests)
    Right(effectiveProfile)
  }
}
