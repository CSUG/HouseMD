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

package com.github.zhongl.housemd

import org.scalatest.FunSpec
import org.mockito.Mockito._
import instrument.Instrumentation
import com.github.zhongl.command.PrintOut
import actors.Actor._
import actors.TIMEOUT
import java.io.ByteArrayOutputStream
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class TraceSpec extends FunSpec with ShouldMatchers {
  describe("Trace") {
    it("should display statistics") {
      val inst = mock(classOf[Instrumentation])
      val out = new ByteArrayOutputStream // System.out

      doReturn(Array(classOf[A])).when(inst).getAllLoadedClasses

      val trace = new Trace(inst, PrintOut(out))

      trace.parse("-t 3 A m".split("\\s+"))

      val host = self
      actor {trace.run(); host ! "exit"}

      var cond = true

      while (cond) {
        host.receiveWithin(10) {
          case TIMEOUT =>
            Advice.onMethodBegin(classOf[A].getName, "m", "()V", new A, Array.empty[AnyRef])
            Advice.onMethodEnd(null)
          case "exit"  => cond = false
        }
      }

      out.toString.split("\n") filter (s => !s.startsWith("INFO") && !s.isEmpty) foreach (_ should startWith("A.m"))
    }
  }

}

class A {
  def m() {}
}
