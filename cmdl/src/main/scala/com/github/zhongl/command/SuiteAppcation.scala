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

import Convertors._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
abstract class SuiteAppcation(name: String, description: String, out: PrintOut)
  extends Command(name, description, out) with Suite with Application {

  private val command   = parameter[String]("command", "sub command name.")
  private val arguments = parameter[Array[String]]("arguments", "sub command arguments.", Some(Array()))

  override def run() {
    run(command(), arguments()) { name => println("Unknown command: " + name) }
  }

  override def help = decorate(helpCommand.list)

  override protected def decorate(list: String) = super.help + "\nCommands:\n" + list

}
