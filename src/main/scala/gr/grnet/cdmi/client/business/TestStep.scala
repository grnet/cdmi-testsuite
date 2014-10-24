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

import gr.grnet.cdmi.client.conf.ClassTestConf

case class TestStep(description: String)(f: (Client, ClassTestConf) ⇒ Unit) {
  def apply(client: Client, conf: ClassTestConf): Unit = f(client, conf)
}

object TestStep {
  def condition(description: String)(condition: ⇒Boolean): TestStep =
    TestStep(description) { (_,_) ⇒ Predef.assert(condition, description) }

  def effect(description: String)(justdoit: ⇒Unit): TestStep =
    TestStep(description) { (_,_) ⇒ justdoit }
}
