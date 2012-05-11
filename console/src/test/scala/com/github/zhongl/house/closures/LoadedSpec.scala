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

package com.github.zhongl.house.closures

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import instrument.Instrumentation
import collection.mutable.ListBuffer
import com.github.zhongl.house.Utils

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class LoadedSpec extends FunSpec with ShouldMatchers {
  describe("LoadedClasses") {
    it("should output loaded classes info") {
      val lines = ListBuffer.empty[String]
      val inst = mock(classOf[Instrumentation])
      val stringClass = classOf[String]

      doReturn(Array(stringClass)).when(inst).getAllLoadedClasses

      new Loaded({lines += _}, inst).apply("java.lang.String")

      lines should contain(stringClass.getName + " -> " + Utils.sourceOf(stringClass))
    }

    it("should output loaded classes info and it's class loader hierarchies") {
      val lines = ListBuffer.empty[String]
      val inst = mock(classOf[Instrumentation])
      val klass = classOf[LoadedClasses]

      doReturn(Array(klass)).when(inst).getAllLoadedClasses

      new LoadedClasses(loaderHierarchies = true).apply(inst) {
        lines += _
      }

      lines should {
        contain(klass.getName + " -> " + Utils.sourceOf(klass)) and
          contain("\t- " + klass.getClassLoader) and
          contain("\t\t- " + klass.getClassLoader.getParent) and
          contain("\t\t\t- " + klass.getClassLoader.getParent.getParent) and
          contain("\t\t\t\t- " + klass.getClassLoader.getParent.getParent.getParent) and
          contain("\t\t\t\t\t- " + klass.getClassLoader.getParent.getParent.getParent.getParent)
      }
    }
  }
}
