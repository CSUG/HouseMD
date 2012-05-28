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
import management.ManagementFactory
import java.io.PrintStream
import java.io.InputStream
import jline.console.completer.Completer
import annotation.tailrec
import java.util.List
import Convertors._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
abstract class CommandSuiteApplication(name: String, version: String, description: String, commandLines: CommandLine*)
  extends CommandLineApplication(name, version, description) {

  protected val prompt   : String      = ManagementFactory.getRuntimeMXBean.getName + ">"
  protected val out      : PrintStream = System.out
  protected val in       : InputStream = System.in
  protected val completer: Completer   = defaultCompleter

  private val command   = parameter[String]("command", "sub command name.")
  private val arguments = parameter[Array[String]]("arguments", "sub command arguments.")

  private val _commandLines = commandLines :+ Help

  override def main(arguments: Array[String]) {
    if (arguments.isEmpty) interact() else super.main(arguments)
  }

  override def run() {
    run(command(), arguments())
  }

  private lazy val defaultCompleter = new Completer {
    private val RE0 = """\s+""".r
    private val RE1 = """\s*(\w+)""".r
    private val RE2 = """\s*(\w+)(.+)""".r

    def complete(buffer: String, cursor: Int, candidates: List[CharSequence]) = buffer match {
      case null | RE0()   =>
        commandLines foreach { c => candidates.add(c.name) }; 0
      case RE1(part)      =>
        commandLines.collect { case c if c.name.startsWith(part) => c.name }.sorted.foreach {candidates.add}; 0
      case RE2(cmd, part) =>
        commandLines find {_.name == cmd} match {
          case Some(cl) if cl.isInstanceOf[Completer] => cl.asInstanceOf[Completer].complete(part, cursor, candidates)
          case _                                      => -1
        }
    }
  }

  private def interact() {
    val reader = new ConsoleReader(in, out)
    reader.setPrompt(prompt)
    reader.addCompleter(completer)

    @tailrec
    def parse(line: String) {
      if (line != null) {
        val array = line.trim.split("\\s+")
        run(array.head, array.tail)
        parse(reader.readLine())
      }
    }

    parse(reader.readLine())
  }

  private def run(name: String, arguments: Array[String]) {
    _commandLines find {_.name == name} match {
      case Some(c) => c.parse(arguments); c.run()
      case None    => throw new IllegalArgumentException("Unknown command: " + name)
    }
  }

  object Help extends CommandLine("help", "show help infomation.") {
    private val command = parameter[String]("command", "sub command name.")

    def run() {}
  }

}
