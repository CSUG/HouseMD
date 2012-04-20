package com.github.zhongl.insider

import java.io.ByteArrayOutputStream
import java.lang.management.ManagementFactory
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class InsideSpec extends FunSpec with ShouldMatchers {
  describe("Inside") {
    it("should compile diagnosis plan to bytecode") (pending)
    it("should list all vm") (pending)
    it("should read input choosed number of vm") (pending)
    it("should attache vm and load agent") (pending)
    it("should format detected VM id and name") {
      Inside.format("123", "java") should be ("\t123\tjava")
    }
    it("should attach VM") {
      val NameRE = """(\d+)@.+""".r
      val NameRE(pid) = ManagementFactory.getRuntimeMXBean().getName()
      val baos =new ByteArrayOutputStream()

      Console.withOut(baos){
        Inside.main(Array(pid))
      }

      val output = baos.toString
      output should startWith ("Attached pid: " + pid + "\n")
      output should endWith ("Detached pid: " + pid + "\n")
    }
  }

}
