package com.github.zhongl

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class CommandLineSpec extends FunSpec with ShouldMatchers {

  import Convertors._

  trait Base extends CommandLine {

    val name        = "app name"
    val version     = "0.1.0"
    val description = "some description"

    protected def run() {}

    def main(arguments: Array[String]) {
      parse(arguments)
    }
  }

  describe("Command Line") {

    it("should parse parameters") {
      val cmdl = new Base {
        val param1 = parameter[Int]("param1", "param1 description")
        val param2 = parameter[String]("param2", "param2 description")
      }

      cmdl main ("123 allen".split("\\s+"))
      cmdl param1() should be(123)
      cmdl param2() should be("allen")
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
        val singleValue = option[String]("--single-value" :: Nil, "set single value", "default")
      }

      cmdl main ("--single-value v".split("\\s+"))
      cmdl singleValue() should be("v")
    }

    it("should get help info") {
      val cmdl = new Base {
        val flag0       = flag("-f" :: "--flag" :: Nil, "enable flag")
        val singleValue = option[String]("--single-value" :: Nil, "set single value", "v")
        val param1      = parameter[String]("param1", "set param1")
        val param2      = parameter[String]("param2", "set param2")
      }

      cmdl.help should be(
        """Version: 0.1.0
          |Usage  : app name [OPTIONS] param1 param2
          |        some description
          |Options:
          |        -f, --flag
          |                enable flag
          |        --single-value=[STRING]
          |                set single value
          |                default: v
          |Parameters:
          |        param1
          |                set param1
          |        param2
          |                set param2
          |""".stripMargin.replaceAll("        ","\t"))
    }

  }

}
