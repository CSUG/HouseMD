package com.github.zhongl.insider

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
}
