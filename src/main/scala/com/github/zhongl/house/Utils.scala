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

import java.io.{ByteArrayOutputStream, InputStream}


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

object Utils {
  def toBytes(stream: InputStream): Array[Byte] = {
    val bytes = new ByteArrayOutputStream
    var read = stream.read
    while (read > -1) {
      bytes.write(read)
      read = stream.read
    }
    bytes.toByteArray
  }

  def sourceOf(klass: Class[_]): String = {
    val file = klass.getResource("/" + klass.getName.replace('.', '/') + ".class").getFile
    extractSource(file)
  }

  def extractSource(file:String) = {
    val FileRE = """(file:)?([^!]+)!?.*""".r
    val FileRE(_, source) = file
    source
  }

}
