package com.github.zhongl.insider

import org.scalatest.FunSpec

class InsiderSpec extends FunSpec {
  describe("Insider") {
    it("could attach a java process") {
      // import scala.sys.process._
      
      // val result = "java com.github.zhongl.insider.Attach" !!
      println("zhongl" + java.lang.System.getProperty("java.class.path"))
    }
  }
}
