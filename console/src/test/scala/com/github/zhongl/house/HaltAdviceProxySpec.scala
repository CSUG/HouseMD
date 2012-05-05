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

import org.scalatest.FunSpec

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class HaltAdviceProxySpec extends FunSpec {
  describe("HaltAdviceProxy") {

    def testHaltAdviceProxy(advice: HaltAdvice, timeout: Int, maxCount: Int) = {
      var cause: Cause = null
      var halt = false

      val proxy = HaltAdviceProxy(advice, timeout, maxCount) {
        case c => halt = true; cause = c
      }

      while (!halt) {
        proxy.enterWith(null)
        proxy.exitWith(null)
      }

      cause
    }

    class NullHaltAdvice extends HaltAdvice {
      def enterWith(context: Context) {}

      def exitWith(context: Context) {}

      def halt() {}
    }

    it("should halt by over max count") {
      expect(Over(1)) {
        testHaltAdviceProxy(new NullHaltAdvice, timeout = Int.MaxValue, maxCount = 1)
      }
    }

    it("should halt by timeout") {
      expect(Timeout(1)) {
        testHaltAdviceProxy(new NullHaltAdvice, timeout = 1, maxCount = Int.MaxValue)
      }
    }

    it("should halt by thrown when call entry") {
      val e = new Exception
      val advice = new NullHaltAdvice {
        override def enterWith(context: Context) {throw e}
      }
      expect(Thrown(e)) {
        testHaltAdviceProxy(advice, timeout = Int.MaxValue, maxCount = Int.MaxValue)
      }
    }

    it("should halt by thrown when call exit") {
      val e = new Exception
      val advice = new NullHaltAdvice {
        override def enterWith(context: Context) {throw e}
      }
      expect(Thrown(e)) {
        testHaltAdviceProxy(advice, timeout = Int.MaxValue, maxCount = Int.MaxValue)
      }
    }

  }
}