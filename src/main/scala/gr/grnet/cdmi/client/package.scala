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

package gr.grnet.cdmi

import java.io.{PrintWriter, StringWriter}

import scala.io.Source

/**
 *
 */
package object client {
  final implicit class RichThrowable(val t: Throwable) extends AnyVal {
    def stringStackTrace(indent: String = "  "): String = {
      val sw = new StringWriter()
      val pw = new PrintWriter(sw)
      t.printStackTrace(pw)
      val string0 = sw.toString

      val lines0 = Source.fromString(string0).getLines()
      if(lines0.hasNext) {
        val lines  = lines0.map(indent + _).toStream
        val string = (lines.head /: lines.tail)(_ + "\n" + _)

        string
      }
      else {
        indent
      }
    }
  }
}
