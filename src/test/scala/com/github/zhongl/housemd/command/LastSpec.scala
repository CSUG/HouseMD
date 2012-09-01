package com.github.zhongl.housemd.command

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.ByteArrayOutputStream
import com.github.zhongl.yascli.PrintOut
import org.mockito.Mockito._
import instrument.Instrumentation

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class LastSpec extends FunSpec with ShouldMatchers {
  describe("Last") {
    it("should show exception stack traces of last error") {
      val bout = new ByteArrayOutputStream()
      val last = new Last(PrintOut(bout))
      val throwable = new Exception()
      last.keep(throwable)
      last.parse(Array.empty[String])
      last.run()
      bout.toString should be(throwable.getStackTrace.map("\t" + _) mkString("", "\n", "\n"))
    }

    it("should show no exception stacks trace with out last error"){
      val bout = new ByteArrayOutputStream()
      val last = new Last(PrintOut(bout))
      last.parse(Array.empty[String])
      last.run()
      bout.toString should be ("There is no error.\n")
    }

  }

}
