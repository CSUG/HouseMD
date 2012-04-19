package com.github.zhongl.insider

import java.io.ByteArrayOutputStream
import java.lang.management.ManagementFactory
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class AttachSpec extends FunSpec with ShouldMatchers {
  describe("Attach") {
    it("should format detected VM id and name") {
      Attach.format("123", "java") should be ("\t123\tjava")
    }
    it("should attach VM") {
      val NameRE = """(\d+)@.+""".r
      val NameRE(pid) = ManagementFactory.getRuntimeMXBean().getName()
      val baos =new ByteArrayOutputStream()

      Console.withOut(baos){
        Attach.main(Array(pid))
      }

      val output = baos.toString
      output should startWith ("Attached pid: " + pid + "\n")
      output should endWith ("Detached pid: " + pid + "\n")
    }
  }

}
