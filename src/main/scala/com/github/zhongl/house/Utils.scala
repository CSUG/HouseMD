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
