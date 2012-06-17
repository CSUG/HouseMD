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
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class ClassSimpleNameCompleterSpec extends FunSpec with ShouldMatchers {

  val c = new ClassSimpleNameCompleter {
    lazy val inst = {
      val m = mock(classOf[Instrumentation])
      doReturn(Array(classOf[BB])).when(m).getAllLoadedClasses
      m
    }
  }

  describe("ClassSimpleNameCompleter") {
    it("should complete nothing without input") {
      val candidates = new java.util.ArrayList[CharSequence]()
      c.complete("   ", 3, candidates) should be(-1)
    }

    it("should complete nothing with unknown prefix") {
      val candidates = new java.util.ArrayList[CharSequence]()
      c.complete("XX", 2, candidates) should be(-1)
    }

    it("should complete BB") {
      val candidates = new java.util.ArrayList[CharSequence]()
      c.complete("B", 1, candidates) should be(0)
      candidates should contain("BB".asInstanceOf[CharSequence])
    }
  }

}

class BB {}
