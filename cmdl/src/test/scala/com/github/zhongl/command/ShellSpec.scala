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
class ShellSpec extends FunSpec with ShouldMatchers {

  class AShell(line: String, out: OutputStream) extends Shell(
    name = "shell",
    description = "a shell example",
    reader = new ConsoleReader(new ByteArrayInputStream(line.getBytes), new PrintStream(out))) {
    protected def commands = helpCommand :: Quit :: Nil
  }

  private val backspace = "\u001B[K"

  private def moveCursor(i: Int) = "\u001B[" + i + "G"

  describe("Shell") {

    it("should get help") {
      val bout = new ByteArrayOutputStream()
      val shell = new AShell("help\n", bout)
      shell main (Array())
      bout.toString should be(
        """shell> help
          |
          |help    display this infomation.
          |quit    terminate the process.
          |
          |shell> """.stripMargin)
    }

    it("should complete help command") {
      val bout = new ByteArrayOutputStream()
      val shell = new AShell("h\t", bout)
      shell main (Array())
      bout.toString should be("shell> h" + moveCursor(8) + backspace + "help")
    }

    it("should complete help command argument") {
      val bout = new ByteArrayOutputStream()
      val shell = new AShell("help q\t", bout)
      shell main (Array())
      bout.toString should be("shell> help q" + moveCursor(13) + backspace + "quit")
    }

    it("should complete nothing") {
      val bout = new ByteArrayOutputStream()
      val shell = new AShell("help help a\t", bout)
      shell main (Array())
      bout.toString should be("shell> help help a")
    }

    it("should complain unknown command name") {
      val bout = new ByteArrayOutputStream()
      val shell = new AShell("unknow\n", bout)
      shell main (Array())
      bout.toString should be(
        """shell> unknow
          |Unknown command: unknow
          |shell> """.stripMargin)
    }

    it("should complain unknown command name in help") {
      val bout = new ByteArrayOutputStream()
      val shell = new AShell("help unknow\n", bout)
      shell main (Array())
      bout.toString should be(
        """shell> help unknow
          |Unknown command: unknow
          |shell> """.stripMargin)
    }
  }

}
