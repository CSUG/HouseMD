package com.github.zhongl.insider

import java.io.ByteArrayInputStream
import org.scalatest.FunSuite

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class UtilsTest extends FunSuite {

  test("toBytes") {
    val array = Array('c'.toByte)
    val stream = new ByteArrayInputStream(array)
    assert(Utils.toBytes(stream) === array)
  }

}
