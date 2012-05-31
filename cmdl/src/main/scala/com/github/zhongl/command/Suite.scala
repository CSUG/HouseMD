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


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
trait Suite {self: Command =>

  protected lazy val helpCommand = new Command("help", "display this infomation.", self.out) {

    private val command = parameter[String]("command", "sub command name.", Some("*"))

    private lazy val length  = (name.length :: commands.map(_.name.length)).max
    private lazy val pattern = "%1$-" + length + "s\t%2$s\n"

    def list = commands.foldLeft[String]("") { (a, c) => a + pattern.format(c.name, c.description) }

    def helpOf(name: String) = commands find (_.name == name) match {
      case Some(c) => c.help + "\n"
      case None    => throw new IllegalArgumentException("Unknown command: " + name)
    }

    def run() {
      try {
        print(command() match {
          case "*" => decorate(list)
          case cmd => helpOf(cmd)
        })
      } catch {case e: IllegalArgumentException => println(e.getMessage) }
    }
  }

  def run(command: String, arguments: Array[String])(handleUnknown: String => Unit) {
    commands find {_.name == command} match {
      case Some(c) => c.parse(arguments); c.run()
      case None    => handleUnknown(command)
    }
  }

  protected def commands: List[Command]

  protected def decorate(list: String): String

}
