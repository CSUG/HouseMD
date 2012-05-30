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
import instrument.Instrumentation
import java.util.concurrent.{TimeUnit, CountDownLatch}

class AgentSpec extends FunSpec {
  describe("Agent") {
    it("should start closure executor") {
      val arguments = "a.jar com.github.zhongl.house.MockExecutor 54321"

      Agent.agentmain(arguments, null)

      Flag.latch.await(1L, TimeUnit.SECONDS)
    }
  }
}

object Flag {
  val latch = new CountDownLatch(1)
}

class MockExecutor(port: Int, inst: Instrumentation) extends Runnable {
  def run() {
    Flag.latch.countDown()
  }
}