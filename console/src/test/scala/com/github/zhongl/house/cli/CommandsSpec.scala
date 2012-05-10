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

package com.github.zhongl.house.cli

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class CommandsSpec extends FunSpec with ShouldMatchers {

  @command(name = "mock", description = "mock a command")
  class Mock {
    def apply() {
      throw new Exception("invoked")
    }
  }

  describe("Commands") {
    it("should get command by name") {
      val command = new Commands(new Mock).command("mock")
      evaluating { command(Array()) } should produce [Exception]
    }

    it("should complain by unknown command name") {
      val exception = evaluating(new Commands().command("unknown")) should produce[IllegalArgumentException]
      exception getMessage() should be ("Unknown command: unknown")
    }
  }

}
