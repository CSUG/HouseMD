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

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class TraceSpec extends FunSpec with ShouldMatchers{
  describe("Trace") {
    it("should iterate trace log") {
      val context = new Context("java.lang.String", "split", false, "", Array(","), Array.empty[StackTraceElement])
      Trace.enterWith(context)
      Thread.sleep(10L)
      context.resultOrException = "result"
      Trace.exitWith(context)
      Trace.next() should include ("java.lang.String.split , result")

      Trace.halt()
      Trace.hasNext should be (false)
    }
  }

}
