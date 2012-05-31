/*
 * Copyright 2012 zhongl
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.zhongl.command

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.{OutputStream, ByteArrayOutputStream}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class SuiteSpec extends FunSpec with ShouldMatchers {

  class ACommand(out: PrintOut) extends Command("cmd", "example.", out) {
    def run() {}
  }

  class ASuiteAppcation(os: OutputStream)
    extends SuiteAppcation(name = "acs", description = "A command suite", out = PrintOut(os))
            with Application {
    override protected lazy val commands = helpCommand :: new ACommand(out) :: Nil
  }

  describe("Suite Application") {

    it("should get help") {
      val bout = new ByteArrayOutputStream()
      val acs = new ASuiteAppcation(bout)
      acs main ("help cmd".split("\\s+"))
      bout.toString should be("Usage: cmd\n\texample.\n")
    }
  }

}
