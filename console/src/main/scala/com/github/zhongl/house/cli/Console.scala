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

import java.io.InputStream
import java.io.OutputStream
import jline.console.ConsoleReader
import instrument.Instrumentation
import scala.annotation.tailrec
import management.ManagementFactory

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Console(in: InputStream, out: OutputStream, instrumentation: Instrumentation) {

  var commands: Commands = _

  private[this] lazy val prompt = ManagementFactory.getRuntimeMXBean.getName + ">"

  def run() {
    val reader = new ConsoleReader(in, out)

    reader.setPrompt(prompt)

    reader.addCompleter(commands)

    @tailrec
    def parse(line: String) {
      line match {
        case null =>
        case _    => execute(line); parse(reader.readLine())
      }
    }

    parse(reader.readLine())
  }

  private[this] def execute(line: String) {
    line split ("\\s+") match {
      case Array()                      =>
      case Array(command)               => commands.execute(command)
      case Array(command, arguments@_*) => commands.execute(command, arguments: _*)
    }
  }


}



