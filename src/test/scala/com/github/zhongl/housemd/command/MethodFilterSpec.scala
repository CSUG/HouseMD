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

package com.github.zhongl.housemd.command

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class MethodFilterSpec extends FunSpec with ShouldMatchers {
  describe("MethodFilter") {
    it("should include R by Runnable+") {
      MethodFilter("Runnable+").filter(classOf[R]) should be(true)
    }

    it("should include R by Runnable+.run") {
      MethodFilter("Runnable+.run").filter(classOf[R]) should be(true)
    }

    it("should include R and run by Runnable+.run") {
      MethodFilter("Runnable+.run").filter(classOf[R], "run") should be(true)
    }

    it("should include R by R.m") {
      MethodFilter("R.m").filter(classOf[R]) should be(true)
    }
  }
}

class R extends Runnable {
  def run() {}

  private def m() {}
}