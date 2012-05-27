package com.github.zhongl

import jline.console.ConsoleReader
import management.ManagementFactory
import java.io.PrintStream
import java.io.InputStream
import jline.console.completer.Completer
import annotation.tailrec

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

trait CommandSuiteApplication {
  protected val commandLines: Array[CommandLine]

  def main(arguments: Array[String]) {
    arguments.toList match {
      case head :: tail => run(head, tail.toArray)
      case Nil          => interact()
    }
  }

  protected def prompt: String = ManagementFactory.getRuntimeMXBean.getName + ">"

  protected def out: PrintStream = System.out

  protected def in: InputStream = System.in

  protected def completer: Completer

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
    commandLines find (_.name == name) match {
      case Some(c) => c.parse(arguments); c.run()
      case None    => throw new IllegalArgumentException("Unknown command: " + name)
    }
  }
}
