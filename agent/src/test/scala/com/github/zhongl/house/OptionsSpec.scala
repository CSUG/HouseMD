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
import java.io.File

class OptionsSpec extends FunSpec with ShouldMatchers {

  val options = Options.parse(
    """class.loader.urls=a.jar:b.jar
       closure.executor.name=com.github.zhongl.house.Executor
       console.address=localhost:54321"""
  )

  def url(s: String) = new File(s).toURI.toURL

  describe("Options") {
    it("should get class loader urls") {
      options.classLoaderUrls() should {
        contain(url("a.jar")) and contain(url("b.jar")) and have size (2)
      }
    }

    it("should get closure executor name") {
      options.mainClass() should be("com.github.zhongl.house.Executor")
    }

    it("should get console address") {
      options.consoleAddress() should be("localhost:54321")
    }
  }
}
