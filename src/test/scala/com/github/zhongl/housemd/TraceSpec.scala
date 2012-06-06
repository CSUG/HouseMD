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

class TraceSpec extends FunSpec with ShouldMatchers {

  def parseAndRun(arguments: String)(verify: (String, File, File) => Unit) {
    val out = new ByteArrayOutputStream
    val inst = mock(classOf[Instrumentation])
    doReturn(Array(classOf[A], classOf[String])).when(inst).getAllLoadedClasses

    val trace = new Trace(inst, PrintOut(out))

    trace.parse(arguments.split("\\s+"))

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

    verify(out.toString, trace.detailFile, trace.stackFile)
  }

  describe("Trace") {
    it("should display statistics") {
      parseAndRun("-t 3 A.m") { (out, detail, stack) =>
        out.split("\n") filter (s => !s.startsWith("INFO") && !s.isEmpty) foreach (_ should startWith("A.m"))
      }
    }

    it("should output invocation details") {
      parseAndRun("-d -t 1 A") { (out, detail, stack) =>
        val date = """\d{4}-\d{2}-\d{2}"""
        val time = """\d{2}:\d{2}:\d{2}"""
        val elapse = """\d+ms"""
        val thread = """\[[^\]]+\]"""
        val name = """com\.github\.zhongl\.housemd\.A\.m"""
        val arguments = """\[\]"""
        val result = "null"
        Source.fromFile(detail).getLines() foreach {
          _ should fullyMatch regex ((date :: time :: elapse :: thread :: name :: arguments :: result :: Nil).mkString(" "))
        }
      }
    }

    it("should end by overlimit") {
      parseAndRun("-l 1 -t 1000 A") { (out, detail, stack) =>
        out.split("\n").dropRight(1).last should be("INFO : Ended by overlimit")
      }
    }

    it("should end tracing by timeout") {
      parseAndRun("-l 100000 -t 1 A") { (out, detail, stack) =>
        out.split("\n").dropRight(1).last should be("INFO : Ended by timeout")
      }
    }

    it("should end tracing by cancel")(pending)

    it("should disable trace final class") {
      parseAndRun("String") { (out, detail, stack) =>
        out.split("\n").head should be("WARN : Can't trace " + classOf[String] + ", because it is final")
      }
    }

    ignore("should only include package com.github") {
      parseAndRun("-p com\\.github .+ m") { (out, detail, stack) =>
        out.split("\n").head should not be ("WARN: Can't trace " + classOf[String] + ", because it is final")
      }
    }

    it("should output invocation stack")(pending)

  }

}

class A {
  def m() {}
}
