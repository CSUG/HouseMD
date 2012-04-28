package com.github.zhongl.insider

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class TraceSpec extends FunSpec with ShouldMatchers{
  describe("Trace") {
    it("should iterate trace log") {
      val context = new Context("java.lang.String", "split", false, "", Array(","), Array.empty[StackTraceElement])
      Trace.enterWith(context)
      Thread.sleep(10L)
      context.resultOrException = "result"
      Trace.exitWith(context)
      Trace.next() should include ("main java.lang.String.split , result")

      Trace.halt()
      Trace.hasNext should be (false)
    }
  }

}
