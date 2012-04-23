package com.github.zhongl.insider

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import scala.Array
import java.io.ByteArrayOutputStream
import management.ManagementFactory

class InsideSpec extends FunSpec with ShouldMatchers {
  describe("Inside") {
    it("should attach VM") {
      val NameRE = """(\d+)@.+""".r
      val NameRE(pid) = ManagementFactory.getRuntimeMXBean().getName()
      val baos =new ByteArrayOutputStream()

      Console.withOut(baos){
        Inside.driveWith(Array("-a","src/test/agent.jar",pid, "String.*"))
      }

      val output = baos.toString
      output should startWith ("Attached pid: " + pid + "\n")
      output should endWith ("Detached pid: " + pid + "\n")
    }
    it("should complain parameter missing and show usage") {
      val thrown = evaluating { Inside.driveWith(Array("123")) } should produce [Exception]
      thrown.getMessage should startWith ("ParameterException: Missing parameter")
    }
    it("should complain no such process") {
      val thrown = evaluating { Inside.driveWith(Array("92091", "m.+")) } should produce [Exception]
      thrown.getMessage should startWith ("IOException: No such process")
    }
    it("should complain invalid regex pattern") {
      val thrown = evaluating { Inside.driveWith(Array("92091", "(")) } should produce [Exception]
      thrown.getMessage should startWith ("ParameterException: Unclosed group near index 1\n(\n ^")
    }
  }

}
