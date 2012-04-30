package com.github.zhongl.house

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

  test("extractSource") {
    assert(Utils.extractSource("file:path!xxx") === "path")
    assert(Utils.extractSource("path") === "path")
  }
}
