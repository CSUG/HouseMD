package com.github.zhongl.housemd.misc;

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:eagleinfly@gmail.com">eagleinfly<a>
 */
class ObjUtilsSpec extends FunSpec with ShouldMatchers {
  describe("ObjUtils") {
    it("should format object in json format") {
      ObjUtils.useJsonFormat()
      ObjUtils.toString(null) should be("null")
      ObjUtils.toString("") should be("\"\"")
      ObjUtils.toString("string") should be("\"string\"")
    }

    it("should format object in toString format") {
      ObjUtils.useToStringFormat()
      ObjUtils.toString(null) should be("null")
      ObjUtils.toString("") should be("")
      ObjUtils.toString("string") should be("string")
    }

    it("should format objects with cycle reference in json format") {
      class A{
        var b: B = null
        var intVal: Int = 0
        var intObj: java.lang.Integer = null
        def setB(b: B) = this.b = b
      }

      class B{
        var a: A = null
        def setA(a: A) = this.a = a
      }

      val a = new A
      a.b = new B
      a.b.a = a
      ObjUtils.useJsonFormat()
      ObjUtils.toString(a) should not(be(null))
    }
  }
}
