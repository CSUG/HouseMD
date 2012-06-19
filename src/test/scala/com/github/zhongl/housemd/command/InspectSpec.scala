package com.github.zhongl.housemd.command

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import instrument.Instrumentation
import com.github.zhongl.yascli.PrintOut
import java.io.ByteArrayOutputStream
import actors.Actor._
import actors.TIMEOUT
import com.github.zhongl.test.{G, A}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class InspectSpec extends FunSpec with ShouldMatchers with AdviceReflection {

  describe("Inspect") {
    it("should display G.i") {
      val inst = mock(classOf[Instrumentation])
      val out = new ByteArrayOutputStream()
      val inspect = new Inspect(inst, PrintOut(out))

      inspect.parse("-l 1 G.i".split("\\s+"))

      doReturn(Array(classOf[G])).when(inst).getAllLoadedClasses

      val host = self
      actor {
        inspect.run();
        host ! "exit"
      }

      var cond = true
      val g = new G
      while (cond) {
        host.receiveWithin(10) {
          case TIMEOUT =>
            invoke(classOf[A].getName, "m", "()V", g, Array.empty[AnyRef], null)
          case "exit" => cond = false
        }
      }

      out.toString.split("\n").filter(l => !l.isEmpty && !l.startsWith("INFO")) should contain("G.i 5 " + g + " " + g.getClass.getClassLoader)
    }
  }

}


