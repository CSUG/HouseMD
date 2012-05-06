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
import actors.Actor
import collection.mutable.ListBuffer

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class EnviromentSpec extends FunSpec {
  case class List()

  class Collector extends Actor {
    val lines = ListBuffer.empty[String]

    def act() {
      loop {
        react {
          case line:String => lines += line
          case List => reply(lines.toList); exit()
        }
      }
    }
  }

  describe("Enviroment") {
    it("should list matched pair") {
      val collector = new Collector
      collector.start()
      new Enviroment(collector, ".*os.*").execute()


    }
  }
}
