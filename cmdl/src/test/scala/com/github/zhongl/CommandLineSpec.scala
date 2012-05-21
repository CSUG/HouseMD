package com.github.zhongl

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class CommandLineSpec extends FunSpec with ShouldMatchers {


  trait Base extends CommandLine {

    val name        = "app name"
    val version     = "0.1.0"
    val description = "some description"

    protected def run() {}
  }

  describe("Command Line") {

    it("should parse parameter") {
      val cmdl = new Base {
        val param = parameter[Int]("param", "param description")
      }

      cmdl main ("123".split("\\s+"))
      cmdl param() should be(123)
    }

    ignore("should support POSIX-style short options") {
      val cmdl = new Base {
        val flag1 = flag("-f" :: Nil, "enable flag1")
        val flag2 = flag("-F" :: Nil, "enable flag2")
      }

      cmdl main ("-fF".split("\\s+"))
      cmdl flag1() should be(true)
      cmdl flag2() should be(true)
    }

    it("should support GNU-style long option") {
      val cmdl = new Base {
        val flag0 = flag("--flag" :: Nil, "enable flag")
      }

      cmdl main ("--flag".split("\\s+"))
      cmdl flag0() should be(true)
    }

    it("should support single-value option") {
      val cmdl = new Base {
        val singleValue = option[String]("--single-value" :: Nil, "set single value")
      }

      cmdl main ("--single-value v".split("\\s+"))
      cmdl singleValue() should be("v")
    }

  }

}
