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

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class MethodFilterCompleterSpec extends FunSpec with ShouldMatchers {
  val c = new MethodFilterCompleter {
    lazy val inst = {
      val m = mock(classOf[Instrumentation])
      doReturn(Array(classOf[CCC], classOf[Runnable])).when(m).getAllLoadedClasses
      m
    }
  }

  describe("MethodFilterCompleter") {
    it("should complete CCC") {
      val candidates = new java.util.ArrayList[CharSequence]()
      c.complete("C", 1, candidates) should be(0)
      candidates should contain("CCC".asInstanceOf[CharSequence])
    }

    it("should complete CCC.m*") {
      val candidates = new java.util.ArrayList[CharSequence]()
      c.complete("CCC.m", 5, candidates) should be(4)
      candidates should {
        contain("<init>".asInstanceOf[CharSequence])
        contain("m1".asInstanceOf[CharSequence])
        contain("m22".asInstanceOf[CharSequence])
      }
    }

    it("should complete Runnable with +") {
      val candidates = new java.util.ArrayList[CharSequence]()
      c.complete("R", 1, candidates) should be(0)
      candidates should contain("Runnable+".asInstanceOf[CharSequence])
    }

    it("should complete Runnable+. ") {
      val candidates = new java.util.ArrayList[CharSequence]()
      c.complete("Runnable+.", 10, candidates) should be(10)
      candidates should contain("run".asInstanceOf[CharSequence])
    }

    it("should complete Runnable+.r ") {
      val candidates = new java.util.ArrayList[CharSequence]()
      c.complete("Runnable+.r", 11, candidates) should be(10)
      candidates should contain("run".asInstanceOf[CharSequence])
    }
  }

}

class CCC {
  def m1() {}

  def m22() {}
}