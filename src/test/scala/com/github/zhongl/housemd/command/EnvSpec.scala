package com.github.zhongl.housemd.command

import org.scalatest.FunSpec
import com.github.zhongl.yascli.PrintOut
import java.io.ByteArrayOutputStream
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class EnvSpec extends FunSpec with ShouldMatchers {
  describe("Env") {
    it("should print env with key name") {
      val out = new ByteArrayOutputStream()
      val env = new Env(PrintOut(out))
      val name = "USER"
      env parse (name.split("\\s+"))
      env.run()
      out.toString should be(name + " = " + System.getenv(name) + "\n")
    }

    it("should list env with regex") {
      val out = new ByteArrayOutputStream()
      val env = new Env(PrintOut(out))
      val name = "USER"
      env parse ("-e US.*".split("\\s+"))
      env.run()
      out.toString should be(name + " = " + System.getenv(name) + "\n")
    }

    it("should complete USER") {
      val candidates = new java.util.ArrayList[CharSequence]()
      new Env(null).complete("US", 2, candidates)
      candidates should {
        have size (1)
        contain("USER".asInstanceOf[CharSequence])
      }
    }
  }
}
