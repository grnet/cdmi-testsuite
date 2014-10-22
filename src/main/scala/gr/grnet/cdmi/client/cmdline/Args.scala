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

package gr.grnet.cdmi.client.cmdline

import com.beust.jcommander.{JCommander, Parameter, Parameters}
import gr.grnet.cdmi.client.Main

/**
 *
 */
object Args {
  def nameOf(cmd: AnyRef): String = {
    val p = cmd.getClass.getAnnotation(classOf[Parameters])
    p.commandNames()(0)
  }

  class GlobalOptions {
    @Parameter(
      names = Array("-h", "-help", "--help"),
      help = true
    )
    val help = false

    @Parameter(
      names = Array("-c"),
      description = "The configuration file the application uses. Use 'default' to load the builtin configuration",
      required = true,
      validateWith = classOf[NotEmptyStringValidator]
    )
    val conf: String = "default"

    @Parameter(
      names = Array("-p"),
      description = "Selects the profile",
      required = true,
      validateWith = classOf[NotEmptyStringValidator]
    )
    val profile: String = "default"

    @Parameter(
      names = Array("-x"),
      description = "Extra, overriding configuration that applies to the chosen profile",
      required = true,
      validateWith = classOf[NotEmptyStringValidator]
    )
    val xconf: String = null
  }

  object ParsedCmdLine {
    val globalOptions = new GlobalOptions
  }


  private[this] def makeJCommander: JCommander = {
    val jc = new JCommander()

    jc.setProgramName(Main.getClass.getName.dropRight(1))

    jc.addObject(ParsedCmdLine.globalOptions)

    jc
  }

  val jc = makeJCommander
}
