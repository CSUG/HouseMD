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

import org.scalatest.matchers.ShouldMatchers
import scala.Array
import org.scalatest.FunSpec

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class HouseMDSpec extends FunSpec with ShouldMatchers {
  describe("HouseMD") {

    it("should complain parameter missing and show usage") {
      val thrown = evaluating { HouseMD.insideWith(Array("123")) } should produce [Exception]
      thrown.getMessage should startWith ("ParameterException: Missing parameter")
    }

    it("should complain no such process") {
      val thrown = evaluating { HouseMD.insideWith(Array("92091", "m.+")) } should produce [Exception]
      thrown.getMessage should startWith ("IOException: No such process")
    }

    it("should complain invalid regex pattern") {
      val thrown = evaluating { HouseMD.insideWith(Array("92091", "(")) } should produce [Exception]
      thrown.getMessage should startWith ("ParameterException: Unclosed group near index 1\n(\n ^")
    }
  }

}
