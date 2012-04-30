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

package com.github.zhongl.house

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class AbstractIteratorSpec extends FunSpec with ShouldMatchers {

  describe("AbstractIterator") {

    it("should hasNext more time without next") {
      val iterator = new AbstractIterator[String] {
        def computeNext = Some("")
      }
      iterator.hasNext should be (true)
      iterator.hasNext should be (true)
    }

    it("should not hasNext after next") {

      var value = Option("")

      val iterator = new AbstractIterator[String] {
        protected def computeNext = value
      }

      iterator.next should be ("")

      value = None
      iterator.next should be (null)
      iterator.hasNext should be (false)
    }
  }

}
