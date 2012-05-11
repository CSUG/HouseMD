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

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.github.zhongl.house.logging.{AssertLog, Level}
import java.util.ArrayList

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class CommandsSpec extends FunSpec with ShouldMatchers {

  abstract class Mock {
    var called = false

    def shouldCalled() {
      assert(called === true)
    }
  }

  @command(name = "mock0", description = "mock a command")
  class Mock0 extends Mock {

    def apply() {
      called = true
    }

  }

  @command(name = "mock1", description = "mock a command")
  class Mock1 extends Mock {
    def apply(
      @argument(name = "boolean", description = "desc") b: Boolean,
      @argument(name = "int", description = "desc") i: Int,
      @argument(name = "long", description = "desc") l: Long,
      @argument(name = "double", description = "desc") d: Double,
      @argument(name = "string", description = "desc") s: String) {
      called = true
      assert(b === true)
      assert(i === 1)
      assert(l === 1000L)
      assert(d === 0.5D)
      assert(s === "s")
    }
  }

  @command(name = "mock1", description = "mock a command")
  class Mock2 extends Mock {
    def apply(
      @option(name = Array("-b", "--boolean"), description = "flag") flag: Boolean = false,
      @argument(name = "arg", description = "desc") arg: String) {
      called = true
      assert(arg === "arg")
    }
  }

  describe("Commands") {
    it("should execute command without argument and get exception") {
      val mock = new Mock0
      val commands = new Commands(mock) with AssertLog with NoneCompleter
      commands.execute("mock0")
      mock.shouldCalled()
    }

    it("should execute command with argument and get exception") {
      val mock = new Mock1
      val commands = new Commands(mock) with AssertLog with NoneCompleter
      commands.execute("mock1", "true", "1", "1000", "0.5", "s")
      mock.shouldCalled()
      commands.shouldNotLogged()
    }

    it("should complain by unknown command name") {
      val commands = new Commands() with AssertLog with NoneCompleter
      commands.execute("unknown")
      commands.shouldLogged(Level.Warn, "Unknown command {}", "unknown")
    }

    it("should complain by illegal argument") {
      pending
    }

    it("should complain by runtime exception of command") {
      pending
    }

    it("should complete all command names") {
      val commands = new Commands() with AssertLog with DefaultCompleter
      val candidates = new ArrayList[CharSequence]()
      val cursor = commands.complete("  ", 2, candidates)
      candidates.get(0) should be("help")
      candidates.get(1) should be("quit")
      candidates.size() should be(2)

      cursor should be(0)
    }

    it("should complete help") {
      val commands = new Commands() with AssertLog with DefaultCompleter
      val candidates = new ArrayList[CharSequence]()
      val cursor = commands.complete("he", 2, candidates)
      candidates.get(0) should be("help")
      candidates.size() should be(1)

      cursor should be(0)

    }

    it("should complete help all arguments") {
      val commands = new Commands() with AssertLog with DefaultCompleter
      val candidates = new ArrayList[CharSequence]()
      val cursor = commands.complete("help ", 5, candidates)
      candidates.get(0) should be("help")
      candidates.get(1) should be("quit")
      candidates.size() should be(2)

      cursor should be(5)

    }

    it("should complete help argument quit") {
      val commands = new Commands() with AssertLog with DefaultCompleter
      val candidates = new ArrayList[CharSequence]()
      val cursor = commands.complete("help qu", 7, candidates)
      candidates.get(0) should be("quit")
      candidates.size() should be(1)

      cursor should be(5)

    }
  }

}


