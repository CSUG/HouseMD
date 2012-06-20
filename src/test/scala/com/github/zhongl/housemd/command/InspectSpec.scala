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
import java.util
import com.github.zhongl.housemd.duck.Duck

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
        inspect.run()
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

  it("should complete G.i") {
    val inst = mock(classOf[Instrumentation])
    val inspect = new Inspect(inst, null)

    doReturn(Array(classOf[G])).when(inst).getAllLoadedClasses

    val candidates = new util.ArrayList[CharSequence]()
    inspect.complete("G.", 2, candidates) should be (2)

    candidates should contain("i".asInstanceOf[CharSequence])
  }

  it("should complete Duck") {
    val inst = mock(classOf[Instrumentation])
    val inspect = new Inspect(inst, null)

    doReturn(Array(classOf[Duck])).when(inst).getAllLoadedClasses

    val candidates = new util.ArrayList[CharSequence]()
    inspect.complete("Duc", 3, candidates) should be (0)

    candidates should contain("Duck".asInstanceOf[CharSequence])
  }

}


