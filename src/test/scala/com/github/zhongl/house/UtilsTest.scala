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

import java.io.ByteArrayInputStream
import org.scalatest.FunSuite
import java.util.concurrent.TimeUnit._

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

  test("convertToTimestamp") {
    def assertTimestamp(hour: Long, minute: Long, second: Long) {
      val millis = HOURS.toMillis(hour) + MINUTES.toMillis(minute) + SECONDS.toMillis(second)
      val Array(h, m, s) = Utils.convertToTimestamp(millis)
      assert(h === hour)
      assert(m === minute)
      assert(s === second)
    }
    assertTimestamp(0, 0, 0)
    assertTimestamp(0, 0, 1)
    assertTimestamp(0, 1, 1)

    assertTimestamp(1, 0, 1)

    assertTimestamp(1, 0, 0)
    assertTimestamp(1, 1, 0)
    assertTimestamp(1, 1, 1)
  }

}
