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
import org.mockito.Mockito._
import instrument.Instrumentation
import com.github.zhongl.yascli.PrintOut
import actors.Actor._
import actors.TIMEOUT
import org.scalatest.matchers.ShouldMatchers
import io.Source
import java.io.{File, ByteArrayOutputStream}
import com.github.zhongl.test._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class TraceSpec extends FunSpec with ShouldMatchers with AdviceReflection {

  def parseAndRun(arguments: String, resultOrException: AnyRef = null)(verify: (String, File, File) => Unit) {
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
          val a = new A
          invoke(classOf[A].getName, "<init>", "()V", a, Array.empty[AnyRef], resultOrException)
          invoke(classOf[A].getName, "m", "()V", a, Array.empty[AnyRef], resultOrException)
        case "exit"  => cond = false
      }
    }

    verify(out.toString, trace.detailFile, trace.stackFile)
  }

  describe("Trace") {
    it("should display statistics") {
      parseAndRun("-t 3 A.m") { (out, detail, stack) =>
        val methodFullName = """[\.\w\(\),\$ <>]+"""
        val objectToString = """[\.\w,@\$ \[\]]+"""
        val number = """\d+\s+"""
        val elapse = """<?\d+ms"""
        out.split("\n").filter(s => !s.startsWith("INFO") && !s.isEmpty).tail foreach {
          _ should fullyMatch regex (methodFullName + objectToString + number + elapse + objectToString)
        }
      }
    }

    it("should output invocation details") {
      parseAndRun("-d -t 1 -l 1 A") { (out, detail, stack) =>
        out.split("\n") should contain("INFO : You can get invocation detail from " + detail)

        val date = """\d{4}-\d{2}-\d{2}"""
        val time = """\d{2}:\d{2}:\d{2}"""
        val elapse = """\d+ms"""
        val thread = """\[[^\]]+\]"""
        val thisObject = """com\.github\.zhongl\.test\.A@[\da-f]+"""
        val name = """com\.github\.zhongl\.test\.A\.(m|<init>)"""
        val arguments = """\[\]"""
        val result = "void"
        Source.fromFile(detail).getLines() foreach {
          _ should fullyMatch regex ((date :: time :: elapse :: thread :: thisObject :: name :: arguments :: result :: Nil)
            .mkString(" "))
        }
      }
    }

    it("should output invocation details with exception stack trace") {
      parseAndRun("-d -t 1 -l 1 A", new Exception) { (out, detail, stack) =>
        val date = """\d{4}-\d{2}-\d{2}"""
        val time = """\d{2}:\d{2}:\d{2}"""
        val elapse = """\d+ms"""
        val thread = """\[[^\]]+\]"""
        val thisObject = """com\.github\.zhongl\.test\.A@[\da-f]+"""
        val name = """com\.github\.zhongl\.test\.A\.(m|<init>)"""
        val arguments = """\[\]"""
        val result = "java\\.lang\\.Exception"
        val log = (date :: time :: elapse :: thread :: thisObject :: name :: arguments :: result :: Nil).mkString(" ")
        Source.fromFile(detail).getLines() foreach {
          _ should {
            fullyMatch regex log or fullyMatch regex ("\\tat .+")
          }
        }
      }
    }

    it("should output invocation stack") {
      parseAndRun("-s -l 1 A") { (out, detail, stack) =>
        out.split("\n") should contain("INFO : You can get invocation stack from " + stack)

        val lines = Source.fromFile(stack).getLines().toList.dropRight(1)
        val head = """com\.github\.zhongl\.test\.A\.m\(\)V call by thread \[[\w-]+\]""".r
        val st = """\t\S+\(\S+:\d+\)""".r
        lines.tail foreach {
          _ match {
            case head() =>
            case st()   =>
            case ""     =>
            case _      => fail()
          }
        }
      }
    }

    it("should only include package com.github") {
      parseAndRun("-p com\\.github String") { (out, detail, stack) =>
        out should not be ("No matched class")
      }
    }

  }

}
