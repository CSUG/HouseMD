package com.github.zhongl.insider

import org.scalatest.FunSpec

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class HaltAdviceProxySpec extends FunSpec {
  describe("HaltAdviceProxy") {

    def testHaltAdviceProxy(advice: Advice, timeout: Int, maxCount: Int) = {
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

    it("should halt by over max count") {
      expect(Over(1)) {
        testHaltAdviceProxy(NullAdvice, timeout = Int.MaxValue, maxCount = 1)
      }
    }

    it("should halt by timeout") {
      expect(Timeout(1)) {
        testHaltAdviceProxy(NullAdvice, timeout = 1, maxCount = Int.MaxValue)
      }
    }

    it("shout halt by thrown when call entry") {
      val e = new Exception
      val advice = new Advice {
        def enterWith(context: Context) {throw e}

        def exitWith(context: Context) {}
      }
      expect(Thrown(e)) {
        testHaltAdviceProxy(advice, timeout = Int.MaxValue, maxCount = Int.MaxValue)
      }
    }

    it("shout halt by thrown when call exit") {
      val e = new Exception
      val advice = new Advice {
        def enterWith(context: Context) {}

        def exitWith(context: Context) {throw e}
      }
      expect(Thrown(e)) {
        testHaltAdviceProxy(advice, timeout = Int.MaxValue, maxCount = Int.MaxValue)
      }
    }

  }
}