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

package com.github.zhongl

import org.scalatest.FunSpec
import java.io.ByteArrayOutputStream
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class ApplicationSpec extends FunSpec with ShouldMatchers {

  object App extends Application("App", "0.1.0", "desc") {

    val param = parameter[String]("param", "parameter")(manifest[String], { value: String =>
      if (value.contains("@")) value else throw new IllegalArgumentException(", it should contains @")
    })

    def run() {}
  }

  val help = """0.1.0
               |Usage: App [OPTIONS] param
               |        desc
               |Options:
               |        -h, --help
               |                show help infomation of this command.
               |Parameters:
               |        param
               |                parameter""".stripMargin.replaceAll("        ", "\t")

  describe("Application") {

    it("should print help by short option") {
      val bout = new ByteArrayOutputStream()
      Console.withOut(bout) {
        App.main(Array("-h"))
      }
      bout.toString should be(help)
    }

    it("should print help by long option") {
      val bout = new ByteArrayOutputStream()
      Console.withOut(bout) {
        App.main(Array("--help"))
      }
      bout.toString should be(help)
    }
  }
}
