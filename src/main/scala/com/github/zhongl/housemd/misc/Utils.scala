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

package com.github.zhongl.housemd.misc

import java.io.{ByteArrayOutputStream, InputStream}
import scala.Array
import java.net.URL


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

object Utils {
  lazy val FileRE = """(file:)?([^!]+)!?.*""".r

  private val noPath = "null"

  private[misc] object File {
    def unapply(url:URL) = url.getFile match {
      case FileRE(_, source) => Some(source)
      case _ => None
    }
  }

  def toBytes(stream: InputStream): Array[Byte] = {
    val bytes = new ByteArrayOutputStream
    var read = stream.read
    while (read > -1) {
      bytes.write(read)
      read = stream.read
    }
    bytes.toByteArray
  }

  def locationOf[T:Manifest] = Option(manifest[T].erasure.getProtectionDomain.getCodeSource) match {
    case Some(codeSource) => Option(codeSource.getLocation)
    case None             => Option(manifest[T].erasure.getResource(resourceNameOf[T]))
  }

  def resourceNameOf[T:Manifest] = "/" + manifest[T].erasure.getName.replace('.', '/') + ".class"

  def sourceOf[T:Manifest] = locationOf[T] match {
    case Some(File(path)) => path
    case None => noPath
  }

  def classNameOf[T: Manifest] = manifest[T].erasure.getName

  def silentClose(closable: {def close()}) {
    if (closable != null) try {closable.close()} catch {case _ => /*ignore*/ }
  }

}
