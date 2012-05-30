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
abstract class Application extends Command {

  val version: String

  private val printHelp = flag("-h" :: "--help" :: Nil, "show help infomation of this command.")

  def main(arguments: Array[String]) {
    try {
      parse(arguments)
      if (printHelp()) out.println(help) else run()
    } catch {
      case UnknownOptionException(option)          => out.println("Unknown option: " + option)
      case MissingParameterException(parameter)    => out.println("Missing parameter: " + parameter)
      case ConvertingException(id, value, explain) => out.println("Invalid " + id + " value: " + value + explain)
    }
  }

  override def help = version + "\n" + super.help
}
