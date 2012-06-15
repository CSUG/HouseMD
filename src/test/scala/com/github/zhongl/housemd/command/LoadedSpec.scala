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
import com.github.zhongl.yascli.PrintOut
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import instrument.Instrumentation
import java.io.ByteArrayOutputStream
import annotation.tailrec

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class LoadedSpec extends FunSpec with ShouldMatchers {
  describe("Loaded") {
    it("should display the source jar of String") {
      parseAndRun("String") {_ should startWith("java.lang.String -> ")}
    }

    it("should display the classloader hierarchies") {
      parseAndRun("-h Loaded") { out =>
        val lines = out.split("\n")
        lines.head should startWith("com.github.zhongl.housemd.command.Loaded -> ")

        @tailrec
        def eq(list: List[String], classLoader: ClassLoader) {
          list match {
            case head :: tail => head should endWith(classLoader.toString); eq(tail, classLoader.getParent)
            case Nil          => // end
          }
        }

        eq(lines.tail.toList, classOf[Loaded].getClassLoader)
      }
    }

    it("should complete class simple name") {
      complete("Lo") { (cursor, candidates) =>
        cursor should be(0)
        candidates should contain("Loaded".asInstanceOf[CharSequence])
      }
      complete("-h Lo") { (cursor, candidates) =>
        cursor should be(3)
        candidates should contain("Loaded".asInstanceOf[CharSequence])
      }
    }

    it("should complete all class simple name") {
      complete("") { (cursor, candidates) =>
        cursor should be(0)
        candidates should not be ('empty)
      }
    }

  }

  def complete(buffer: String)(verify: (Int, java.util.List[CharSequence]) => Unit) {

    val inst = mock(classOf[Instrumentation])
    val out = new ByteArrayOutputStream()

    doReturn(Array(classOf[String], classOf[Loaded])).when(inst).getAllLoadedClasses

    val loaded = new Loaded(inst, PrintOut(out))

    val candidates = new java.util.ArrayList[CharSequence]()
    val cursor = loaded.complete(buffer, buffer.length, candidates)
    verify(cursor, candidates)
  }


  def parseAndRun(arguments: String)(verify: String => Unit) {
    val inst = mock(classOf[Instrumentation])
    val out = new ByteArrayOutputStream()

    doReturn(Array(classOf[String], classOf[Loaded])).when(inst).getAllLoadedClasses

    val loaded = new Loaded(inst, PrintOut(out))

    loaded.parse(arguments.split("\\s+"))
    loaded.run()
    verify(out.toString)
  }

}
