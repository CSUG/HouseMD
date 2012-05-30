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
import java.io.{OutputStream, ByteArrayInputStream, PrintStream, ByteArrayOutputStream}
import jline.console.ConsoleReader

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class CommandSuiteSpec extends FunSpec with ShouldMatchers {

  class ACommandSuite(line: String, out: OutputStream) extends CommandSuite(
    name = "acs",
    version = "0.1.0",
    description = "A command suite",
    reader = new ConsoleReader(new ByteArrayInputStream(line.getBytes),new PrintStream(out)),
    commands = Set.empty[Command]
  )

  private val backspace = "\u001B[K"

  private def moveCursor(i: Int) = "\u001B[" + i + "G"

  describe("Command suite") {

    it("should run as non-interactive") {
      val bout = new ByteArrayOutputStream()
      val acs = new ACommandSuite("", bout)
      acs main ("help quit".split("\\s+"))
      bout.toString should be("\nUsage: quit\n    terminate the process.\n\n")
    }

    it("should run as interactive") {
      val bout = new ByteArrayOutputStream()
      val acs = new ACommandSuite("help\n", bout)
      acs main (Array())
      bout.toString should be(
        """acs> help
          |
          |help    display this infomation.
          |quit    terminate the process.
          |
          |acs> """.stripMargin)
    }

    it("should complete help command") {
      val bout = new ByteArrayOutputStream()
      val acs = new ACommandSuite("h\t", bout)
      acs main (Array())
      bout.toString should be("acs> h" + moveCursor(6) + backspace + "help")
    }

    it("should complete help command argument") {
      val bout = new ByteArrayOutputStream()
      val acs = new ACommandSuite("help q\t", bout)
      acs main (Array())
      bout.toString should be("acs> help q" + moveCursor(11) + backspace + "quit")
    }

    it("should complete nothing") {
      val bout = new ByteArrayOutputStream()
      val acs = new ACommandSuite("help help a\t", bout)
      acs main (Array())
      bout.toString should be("acs> help help a")
    }

    it("should complain unknown command name") {
      val bout = new ByteArrayOutputStream()
      val acs = new ACommandSuite("unknow\n", bout)
      acs main (Array())
      bout.toString should be(
        """acs> unknow
          |Unknown command: unknow
          |acs> """.stripMargin)
    }

    it("should complain unknown command name in help") {
      val bout = new ByteArrayOutputStream()
      val acs = new ACommandSuite("help unknow\n", bout)
      acs main (Array())
      bout.toString should be(
        """acs> help unknow
          |Unknown command: unknow
          |acs> """.stripMargin)
    }
  }

}
