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

package com.github.zhongl

import jline.console.ConsoleReader
import java.io.PrintStream
import java.io.InputStream
import annotation.tailrec
import java.util.List
import Convertors._
import jline.console.completer.{NullCompleter, Completer}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
abstract class CommandSuiteApplication(name: String, version: String, description: String, commands: Command*)
  extends CommandLineApplication(name, version, description) {

  protected val prompt   : String      = name + ">"
  protected val out      : PrintStream = System.out
  protected val in       : InputStream = System.in
  protected val completer: Completer   = new DefaultCompleter

  private val command   = parameter[String]("command", "sub command name.")
  private val arguments = parameter[Array[String]]("arguments", "sub command arguments.")

  private val _commands = commands :+ Help

  override def main(arguments: Array[String]) { if (arguments.isEmpty) interact() else super.main(arguments) }

  override def run() { run(command(), arguments()) }

  private def interact() {
    val reader = new ConsoleReader(in, out)
    reader.setPrompt(prompt)
    reader.addCompleter(completer)

    @tailrec
    def parse(line: String) {
      if (line == null) return
      val array = line.trim.split("\\s+")
      run(array.head, array.tail)
      parse(reader.readLine())
    }

    parse(reader.readLine())
  }

  private def run(name: String, arguments: Array[String]) {
    _commands find {_.name == name} match {
      case Some(c) => c.parse(arguments); c.run()
      case None    => throw new IllegalArgumentException("Unknown command: " + name)
    }
  }

  object Help extends Command("help", "display this infomation.") {
    private val command = parameter[String]("command", "sub command name.")

    def run() {}
  }

  object Quit extends Command("quit", "terminate the process.") {
    private val command = parameter[String]("command", "sub command name.")

    def run() {}
  }

  protected class DefaultCompleter extends Completer {

    import collection.JavaConversions._

    private val RE0 = """\s+""".r
    private val RE1 = """\s*(\w+)""".r
    private val RE2 = """\s*(\w+)(.+)""".r

    def complete(buffer: String, cursor: Int, candidates: List[CharSequence]) = buffer match {
      case null | RE0() => candidates.addAll(commandNames); 0
      case RE1(p)       => candidates.addAll(commandNamesStartsWith(p)); 0
      case RE2(n, p)    => completerOfCommand(n).complete(p, cursor, candidates)
    }

    def completerOfCommand(name: String): Completer = _commands find {_.name == name} match {
      case Some(cl) if cl.isInstanceOf[Completer] => cl.asInstanceOf[Completer]
      case _                                      => NullCompleter.INSTANCE
    }

    def commandNamesStartsWith(prefix: String): List[_ <: CharSequence] =
      _commands.collect { case cl if cl.name.startsWith(prefix) => cl.name }.sorted

    def commandNames: List[_ <: CharSequence] = _commands.map(_.name).sorted
  }

}
