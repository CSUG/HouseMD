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

import java.io.PrintStream

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

object CommandLineParser {
  def apply(out: PrintStream = System.out) = new CommandLineParser(out)
}

class CommandLineParser(out: PrintStream) {

  def add(klass: Class[_]) {}

  def parse(line: String) = _

  trait Command

  object Help {

    @command(name = "help", description = "show help infomation of the command or all commands")
    def apply(@argument(name = "command", description = "command name to show information")command: String = "*") {
      command match {
        case "*" => list()
        case _ => usage(command)
      }
    }

    private[this] def list() {
      // TODO
    }

    private[this] def usage(command: String) {
      // TODO
    }
  }

  object Quit {

    @command(name = "quit", description = "quit the console")
    def apply() {
      sys.exit()
    }
  }

}

