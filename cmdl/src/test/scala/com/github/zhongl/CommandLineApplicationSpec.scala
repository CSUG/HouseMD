package com.github.zhongl

import org.scalatest.FunSpec
import java.io.ByteArrayOutputStream
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class CommandLineApplicationSpec extends FunSpec with ShouldMatchers{

  object App extends CommandLineApplication {
    val name        = "App"
    val version     = "0.1.0"
    val description = "desc"

    val param = parameter[String]("param", "parameter") { value =>
      if (value.contains("@")) value else throw new IllegalArgumentException(", it should contains @")
    }

    def run() {}
  }

  val help = """Version: 0.1.0
               |Usage  : App [OPTIONS] param
               |        desc
               |Options:
               |        -h, --help
               |                show help infomation of this command.
               |Parameters:
               |        param
               |                parameter
               |""".stripMargin.replaceAll("        ", "\t")

  describe("Command line application") {
    it("should print help by short option") {
      val bout = new ByteArrayOutputStream()
      Console.withOut(bout) {
        App.main(Array("-h"))
      }
      bout.toString() should be (help)
    }

    it("should print help by long option") {
      val bout = new ByteArrayOutputStream()
      Console.withOut(bout) {
        App.main(Array("--help"))
      }
      bout.toString() should be (help)
    }
  }
}
