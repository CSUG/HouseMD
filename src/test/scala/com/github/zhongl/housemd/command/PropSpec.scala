package com.github.zhongl.housemd.command

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.ByteArrayOutputStream
import com.github.zhongl.yascli.PrintOut

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class PropSpec extends FunSpec with ShouldMatchers {
  describe("Prop") {
    it("should print property with key name") {
      val out = new ByteArrayOutputStream()
      val prop = new Prop(PrintOut(out))
      val name = "java.home"
      prop parse (name.split("\\s+"))
      prop.run()
      out.toString should be(name + " = " + System.getProperty(name) + "\n")
    }

    it("should list properties with regex") {
      val out = new ByteArrayOutputStream()
      val prop = new Prop(PrintOut(out))
      val name = "java.home"
      prop parse ("-e java\\.ho.*".split("\\s+"))
      prop.run()
      out.toString should be(name + " = " + System.getProperty(name) + "\n")
    }

    it("should complete java.home") {
      val candidates = new java.util.ArrayList[CharSequence]()
      new Prop(null).complete("java.ho", 7, candidates)
      candidates should {
        have size (1)
        contain("java.home".asInstanceOf[CharSequence])
      }
    }
  }
}
