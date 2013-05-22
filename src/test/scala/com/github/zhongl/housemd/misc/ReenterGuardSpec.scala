package com.github.zhongl.housemd.misc

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class ReenterGuardSpec extends FunSpec with ShouldMatchers {
  describe("ReenterGuard") {
    it("should fail to reenter without leave") {
      ReenterGuard enter() should be(true)
      ReenterGuard enter() should be(false)
    }
    it("should reenter after leave") {
      ReenterGuard leave()
      ReenterGuard enter() should be(true)
      ReenterGuard leave()
      ReenterGuard enter() should be(true)
    }
  }
}
