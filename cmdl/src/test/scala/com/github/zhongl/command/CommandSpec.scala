/*
 * Copyright 2012 zhongl
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.zhongl.command

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.File

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class CommandSpec extends FunSpec with ShouldMatchers {

  import Convertors._

  abstract class Base extends Command("app name","some description", System.out) {

    def run() {}

    def main(arguments: Array[String]) {
      parse(arguments)
    }
  }

  describe("Command") {

    it("should parse parameters") {
      val command = new Base {
        val param1 = parameter[Int]("param1", "param1 description")
        val param2 = parameter[String]("param2", "param2 description")
      }

      command main ("123 allen".split("\\s+"))
      command param1() should be(123)
      command param2() should be("allen")
    }

    it("should support var length paramters") {
      val command = new Base {
        val param1 = parameter[Int]("param1", "param1 description")
        val param2 = parameter[Array[String]]("param2", "param2 description")
      }

      command main ("123 allen john".split("\\s+"))
      command param1() should be(123)
      command param2() should be(Array("allen", "john"))
    }

    it("should support optional parameter") {
      val command = new Base {
        val param = parameter[String]("param", "desc", Some(""))
      }

      command main (Array())
      command param() should be("")
    }

    ignore("should support POSIX-style short options") {
      val command = new Base {
        val flag1 = flag("-f" :: Nil, "enable flag1")
        val flag2 = flag("-F" :: Nil, "enable flag2")
      }

      command main ("-fF".split("\\s+"))
      command flag1() should be(true)
      command flag2() should be(true)
    }

    it("should support GNU-style long option") {
      val command = new Base {
        val flag0 = flag("--flag" :: Nil, "enable flag")
      }

      command main ("--flag".split("\\s+"))
      command flag0() should be(true)
    }

    it("should support single-value option") {
      val command = new Base {
        val singleValue = option[String]("--single-value" :: Nil, "set single value", "default")
      }

      command main ("--single-value v".split("\\s+"))
      command singleValue() should be("v")
    }

    it("should get help info") {
      val command = new Base {
        val flag0       = flag("-f" :: "--flag" :: Nil, "enable flag")
        val singleValue = option[String]("--single-value" :: Nil, "set single value", "v")
        val param1      = parameter[String]("param1", "set param1")
        val param2      = parameter[String]("param2", "set param2")
      }

      command.help should be(
        """Usage: app name [OPTIONS] param1 param2
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
          |                set param2""".stripMargin.replaceAll("        ", "\t"))
    }

    it("should get help without options and parameters") {
      val command = new Base {}

      command.help should be(
        """Usage: app name
          |        some description""".stripMargin.replaceAll("        ", "\t"))
    }

    it("should get help indicate var-length optional parameter") {
      val command = new Base {
        val param = parameter[Array[String]]("param", "var-length optional param", Some(Array()))
      }
      command.help should be(
        """Usage: app name [param...]
          |        some description
          |Parameters:
          |        param
          |                var-length optional param""".stripMargin.replaceAll("        ", "\t"))

    }

    it("should complain unknown option") {
      val command = new Base {}

      val exception = evaluating {command main ("-u".split("\\s+"))} should produce[UnknownOptionException]
      exception.name should be("-u")
    }

    it("should complain missing parameter") {
      val command = new Base {
        val param = parameter[String]("param", "set param")
      }
      command main (Array())
      val exception = evaluating {command param()} should produce[MissingParameterException]
      exception.name should be("param")
    }

    it("should complain converting error") {
      val command = new Base {
        implicit val toFile = {
          value: String =>
            val file = new File(value)
            if (file.exists()) file else throw new IllegalArgumentException(", it should be an existed file")
        }
        val file = option[File]("--file" :: Nil, "set a file", new File("default"))
      }
      command main ("--file nonexist".split("\\s+"))
      val exception = evaluating {command file()} should produce[ConvertingException]
      exception.name should be("--file")
      exception.value should be("nonexist")
      exception.explain should be(", it should be an existed file")
    }
  }

}
