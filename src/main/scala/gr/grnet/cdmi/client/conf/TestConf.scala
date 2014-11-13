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

import com.typesafe.config.{ConfigFactory, Config}
import scala.collection.JavaConverters._

/**
 *
 */
case class TestConf(
  className: String,
  `http-headers`: List[Header],
  specific: Config
)

object TestConf {
  def parseTest(testConfig: Config): TestConf = {
    val className = testConfig.getString(Key.className)

    val headerConfigs =
      if(testConfig.hasPath(Key.`http-headers`))
        testConfig.getConfigList(Key.`http-headers`).asScala.toList
      else
        ConfigFactory.empty() :: Nil

    val `http-headers` = headerConfigs.map(parseHeader)

    val specific =
      if(testConfig.hasPath(Key.specific))
        testConfig.getConfig(Key.specific)
      else
        ConfigFactory.empty()

    TestConf(className, `http-headers`, specific)
  }

  def parseTests(config: Config): List[TestConf] = {
    val testConfigs =
      if(config.hasPath(Key.tests))
        config.getConfigList(Key.tests).asScala.toList
      else
        ConfigFactory.empty() :: Nil

    val testConf = testConfigs.map(parseTest)
    testConf
  }
}
