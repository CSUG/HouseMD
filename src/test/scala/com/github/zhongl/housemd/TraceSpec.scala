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
import com.github.zhongl.yascli.PrintOut
import actors.Actor._
import actors.TIMEOUT
import org.scalatest.matchers.ShouldMatchers
import io.Source
import java.io.{File, ByteArrayOutputStream}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class TraceSpec extends FunSpec with ShouldMatchers with AdviceReflection {

  def parseAndRun(arguments: String)(verify: (String, File, File) => Unit) {
    val out = new ByteArrayOutputStream
    val inst = mock(classOf[Instrumentation])
    doReturn(Array(classOf[A], classOf[String])).when(inst).getAllLoadedClasses

    val trace = new Trace(inst, PrintOut(out))

    trace.parse(arguments.split("\\s+"))

    trace.detailFile.delete()
    trace.stackFile.delete()

    val host = self
    actor {trace.run(); host ! "exit"}

    var cond = true
    while (cond) {
      host.receiveWithin(10) {
        case TIMEOUT =>
          invoke(classOf[A].getName, "m", "()V", new A, Array.empty[AnyRef], null)
        case "exit"  => cond = false
      }
    }

    verify(out.toString, trace.detailFile, trace.stackFile)
  }

  describe("Trace") {
    it("should display statistics") {
      parseAndRun("-t 3 A.m") { (out, detail, stack) =>
        val methodFullName = """[\.\w\(\),\$ ]+"""
        val objectToString = """[\.\w,@\$ ]+"""
        val number = """\d+\s+"""
        val elapse = """<?\d+ms"""
        out.split("\n") filter (s => !s.startsWith("INFO") && !s.isEmpty) foreach {
          _ should fullyMatch regex (methodFullName + objectToString + number + elapse + objectToString)
        }
      }
    }

    it("should output invocation details") {
      parseAndRun("-d -t 1 A") { (out, detail, stack) =>
        val date = """\d{4}-\d{2}-\d{2}"""
        val time = """\d{2}:\d{2}:\d{2}"""
        val elapse = """\d+ms"""
        val thread = """\[[^\]]+\]"""
        val thisObject = """com\.github\.zhongl\.housemd\.A@[\da-f]+"""
        val name = """com\.github\.zhongl\.housemd\.A\.m"""
        val arguments = """\[\]"""
        val result = "void"
        Source.fromFile(detail).getLines() foreach {
          _ should fullyMatch regex ((date :: time :: elapse :: thread :: thisObject :: name :: arguments :: result :: Nil)
            .mkString(" "))
        }
      }
    }

    it("should output invocation stack")(pending)

  }

}
