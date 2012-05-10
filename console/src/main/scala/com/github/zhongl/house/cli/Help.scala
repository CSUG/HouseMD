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

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

@command(name = "help", description = "show help infomation of the command or all commands")
class Help(val output: String => Unit, commands: Commands) {

  def apply(@argument(name = "command", description = "command name to show information") command: String = "*") {
    command match {
      case "*" => list()
      case _   => usage(command)
    }
  }

  private[this] def list() {
    // TODO
  }

  private[this] def usage(command: String) {
    // TODO
  }

  case class Arguments(@argument(name = "command", description = "command name to show information") command: String = "*")

}
