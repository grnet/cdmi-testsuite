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

/**
 *
 */
case class FullConf(
  `cdmi-spec-version`: String,
  `cdmi-header`: Header,
//  `std-headers`: List[Header],
//  `extra-headers`: List[Header],
  `all-headers`: List[Header],
  `root-uri`: URI,
  tests: List[TestConf]
)

object FullConf {
  def parse(config: Config): FullConf = {
    val `cdmi-spec-version` = config.getString(Key.`cdmi-spec-version`)
    val `cdmi-header` = parseHeader(config.getConfig(Key.`cdmi-header`))
//    val `std-headers` = config.getConfigList(Key.`std-headers`).asScala.toList.map(parseHeader)
//    val `extra-headers` = config.getConfigList(Key.`extra-headers`).asScala.toList.map(parseHeader)
    val `all-headers` = config.getConfigList(Key.`all-headers`).asScala.toList.map(parseHeader)
    val `root-uri` = new URI(config.getString(Key.`root-uri`))
    val tests = TestConf.parseTests(config)

    val fullConf = FullConf(
      `cdmi-spec-version`,
      `cdmi-header`,
//      `std-headers`,
//      `extra-headers`,
      `all-headers`,
      `root-uri`,
      tests
    )

    fullConf
  }
}
