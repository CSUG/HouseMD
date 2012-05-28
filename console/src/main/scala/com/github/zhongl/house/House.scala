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

package com.github.zhongl.house

import com.github.zhongl.CommandLineApplication
import java.io.File


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object House extends CommandLineApplication("house", "0.2.0", "a runtime diagnosis tool of JVM.") {

  implicit private val string2Port = { value: String =>
      val p = value.toInt
      if (p > 1024 && p < 65536) p else throw new IllegalArgumentException(", it should be between 1025 and 65535")
  }

  implicit private val string2File = { value: String =>
    val file = new File(value)
    if (file.exists() && file.isFile) file else throw new IllegalArgumentException(", it should be an existed file")
  }

  private val defaultFile = new File("agent.jar")

  private val debug = flag("-d" :: "--debug" :: Nil, "enable debug mode.")
  private val port  = option[Int]("-p" :: "--port" :: Nil, "set console local socket server port number.", 54321)
  private val agent = option[File]("-a" :: "--agent" :: Nil, "set java agent jar file.", defaultFile)
  private val pid   = parameter[String]("pid", "id of process to be diagnosing.")

  def run() {}
}
