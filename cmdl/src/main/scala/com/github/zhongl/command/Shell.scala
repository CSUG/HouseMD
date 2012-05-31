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

import jline.console.ConsoleReader
import annotation.tailrec
import java.util.List
import jline.console.completer.{NullCompleter, Completer}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
abstract class Shell(
  name: String,
  description: String,
  reader: ConsoleReader = new ConsoleReader(System.in, System.out))
  extends Command(name, description, PrintOut(reader)) with Suite {

  final def main(arguments: Array[String]) { interact() }

  override final def run() {}

  protected def prompt: String = name + "> "

  override protected def decorate(list: String) = "\n" + list + "\n"

  private def interact() {
    reader.setPrompt(prompt)
    reader.addCompleter(DefaultCompleter)

    @tailrec
    def parse(line: String) {
      if (line == null) return
      val array = line.trim.split("\\s+")
      try {
        run(array.head, array.tail) { name => println("Unknown command: " + name) }
      } catch {
        case e: QuitException => return
      }
      parse(reader.readLine())
    }

    parse(reader.readLine())
  }

  object Quit extends Command("quit", "terminate the process.", PrintOut(reader)) {
    def run() { throw new QuitException }
  }

  class QuitException extends Exception

  trait CommandCompleter extends Completer {

    import collection.JavaConversions._

    private val RE0 = """\s+""".r
    private val RE1 = """\s*(\w+)""".r
    private val RE2 = """\s*(\w+)(.+)""".r

    def complete(buffer: String, cursor: Int, candidates: List[CharSequence]) = buffer match {
      case null | RE0() => candidates.addAll(commandNames); cursor
      case RE1(p)       => candidates.addAll(commandNamesStartsWith(p)); cursor - p.length
      case RE2(n, p)    => argumentComplete(n, p, cursor, candidates)
    }

    protected def argumentComplete(name: String, prefix: String, cursor: Int, candidates: List[CharSequence]): Int

    private def commandNamesStartsWith(prefix: String): List[_ <: CharSequence] =
      commands.collect { case cl if cl.name.startsWith(prefix) => cl.name }.sorted

    private def commandNames: List[_ <: CharSequence] = commands.map(_.name).sorted
  }

  object HelpCompleter extends CommandCompleter {
    protected def argumentComplete(name: String, prefix: String, cursor: Int, candidates: List[CharSequence]) = -1
  }

  object DefaultCompleter extends CommandCompleter {

    protected def argumentComplete(name: String, prefix: String, cursor: Int, candidates: List[CharSequence]) =
      completerOfCommand(name).complete(prefix, cursor, candidates)

    private def completerOfCommand(name: String): Completer = commands find {_.name == name} match {
      case Some(c) if c.isInstanceOf[Completer] => c.asInstanceOf[Completer]
      case Some(c) if c == helpCommand          => HelpCompleter
      case _                                    => NullCompleter.INSTANCE
    }
  }

}
