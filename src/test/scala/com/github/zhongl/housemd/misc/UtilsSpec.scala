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

import java.io.ByteArrayInputStream
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import Utils._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class UtilsSpec extends FunSpec with ShouldMatchers{

  describe("Untils") {
    it("should convert input stream to bytes") {
      val array = Array('c'.toByte)
      val stream = new ByteArrayInputStream(array)
      toBytes(stream) should be(array)
    }

    it("should get source of a class") {
      val location = classOf[UtilsSpec].getProtectionDomain.getCodeSource.getLocation
      sourceOf[UtilsSpec] should be(File.unapply(location).get)
    }

    it("should get source of class which CodeSource is null") {
      val location = classOf[String].getResource(resourceNameOf[String])
      sourceOf[String] should be(File.unapply(location).get)
    }
  }

}
