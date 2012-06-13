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
import com.github.zhongl.test._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class TransformerSpec extends FunSpec with ShouldMatchers with AdviceReflection{

  class TransformerConcrete(val inst: Instrumentation, out: PrintOut)
    extends Command("concrete", "test mock.", out) with Transformer {
    protected def hook = new Hook() {}
  }

  def parseAndRun(arguments: String)(verify: (String) => Unit) {
    val out = new ByteArrayOutputStream
    val inst = mock(classOf[Instrumentation])

    doReturn(Array(classOf[I], classOf[A], classOf[F], classOf[String], classOf[Duck])).when(inst).getAllLoadedClasses

    val concrete = new TransformerConcrete(inst, PrintOut(out))

    concrete.parse(arguments.split("\\s+"))

    val host = self
    actor {concrete.run(); host ! "exit"}

    var cond = true
    while (cond) {
      host.receiveWithin(10) {
        case TIMEOUT =>
          invoke(classOf[A].getName, "m", "()V", new A, Array.empty[AnyRef],null)
          invoke(classOf[F].getName, "m", "()V", new A, Array.empty[AnyRef],null)
        case "exit"  => cond = false
      }
    }

    verify(out.toString)
  }


  describe("Transformer") {
    it("should probe final class") {
      parseAndRun("-l 1 F.m") { out =>
        out.split("\n").filter(!_.startsWith("INFO")) foreach {
          _ should fullyMatch regex ("F.+")
        }
      }
    }

    it("should reset by overlimit") {
      parseAndRun("-l 1 -t 3 A") { out =>
        out.split("\n").dropRight(1).last should be("INFO : Ended by overlimit")
      }
    }

    it("should reset by timeout") {
      parseAndRun("-l 100000 -t 1 A") { out =>
        out.split("\n").dropRight(1).last should be("INFO : Ended by timeout")
      }
    }

    it("should not probe class loaded by boot classloader") {
      parseAndRun("String") { out =>
        out.split("\n").head should be("WARN : Skip " + classOf[String] + " loaded from bootclassloader.")
      }
    }

    it("should not probe interface") {
      parseAndRun("I") { out =>
        out.split("\n") should {
          contain("WARN : Skip " + classOf[I])
          contain("No matched class")
        }
      }
    }

    it("should not probe classes belongs to HouseMD") {
      parseAndRun("Duck") { out =>
        out.split("\n") should {
          contain("WARN : Skip " + classOf[Duck] +" belongs to HouseMD.")
          contain("No matched class")
        }
      }
    }

    it("should probe F by I+") {
      parseAndRun("-l 1 I+") { out =>
        val withoutInfo = out.split("\n").filter(!_.startsWith("INFO"))
        withoutInfo.head should be("WARN : Skip " + classOf[I])
        withoutInfo.tail foreach {
          _ should fullyMatch regex ("F.+")
        }
      }
    }

    it("should only include package com.github") {
      parseAndRun("-p com\\.github String") { out =>
        out should not be ("No matched class")
      }
    }

  }

}
