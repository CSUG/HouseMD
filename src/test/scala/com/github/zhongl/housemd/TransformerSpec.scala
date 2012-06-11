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
import org.scalatest.matchers.ShouldMatchers
import java.io.ByteArrayOutputStream
import org.mockito.Mockito._
import instrument.Instrumentation
import actors.Actor._
import actors.TIMEOUT
import com.github.zhongl.yascli.{Command, PrintOut}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class TransformerSpec extends FunSpec with ShouldMatchers {
  class TransformerConcrete(val inst:Instrumentation, out: PrintOut) extends Command("concrete", "test mock.", out)with Transformer{
    protected def hook = new Hook() {}
  }

  def parseAndRun(arguments: String)(verify: (String) => Unit) {
    val out = new ByteArrayOutputStream
    val inst = mock(classOf[Instrumentation])
    doReturn(Array(classOf[A], classOf[String])).when(inst).getAllLoadedClasses

    val concrete = new TransformerConcrete(inst, PrintOut(out))

    concrete.parse(arguments.split("\\s+"))

    val host = self
    actor {concrete.run(); host ! "exit"}

    var cond = true
    while (cond) {
      host.receiveWithin(10) {
        case TIMEOUT =>
          Advice.onMethodBegin(classOf[A].getName, "m", "()V", new A, Array.empty[AnyRef])
          Advice.onMethodEnd(null)
        case "exit"  => cond = false
      }
    }

    verify(out.toString)
  }

  describe("Transformer") {

    it("should end by overlimit") {
      parseAndRun("-l 1 -t 1000 A") { out =>
        out.split("\n").dropRight(1).last should be("INFO : Ended by overlimit")
      }
    }

    it("should end tracing by timeout") {
      parseAndRun("-l 100000 -t 1 A") { out =>
        out.split("\n").dropRight(1).last should be("INFO : Ended by timeout")
      }
    }

    it("should end tracing by cancel")(pending)

    it("should disable trace class loaded by boot classloader") {
      parseAndRun("String") { out =>
        out.split("\n").head should be("WARN : Failed to probe " + classOf[String] + " because of java.lang.NullPointerException: classloader is null.")
      }
    }

    ignore("should only include package com.github") {
      parseAndRun("-p com\\.github .+ m") { out =>
        out.split("\n").head should not be ("WARN: Can't trace " + classOf[String] + ", because it is final")
      }
    }

  }

}

class A {
  def m() {}
}

