package com.github.zhongl.insider

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class AttachSpec extends FunSpec with ShouldMatchers {
  describe("Attach") {
    it("should format detected VM id and name") {
      Attach.format("123", "java") should be ("\t123\tjava")
    }
    it("should attach VM") {
      // Attach.main(Array())
      pending
    }
  }

}
