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

package com.github.zhongl.housemd.misc

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.github.zhongl.housemd.instrument.Advice

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class ReflectionsSpec extends FunSpec with ShouldMatchers {
  describe("Reflection") {
    it("should define Advice") {
      Reflections.loadOrDefine(classOf[Advice], new ClassLoader() {
        override def loadClass(name: String) = {
          if (name == classOf[Advice].getName) throw new ClassNotFoundException()
          else super.loadClass(name)
        }
      })
    }

    it("shoulde get object native string"){
      val o = new Object()
      Reflections.toNativeString(o) should be (o.toString)
    }

    it("shoulde get or force to native string") {
      val o = new Object()
      Reflections.getOrForceToNativeString(o) should be (o.toString)
    }

    it("should force to native string") {
      val s = "hello"
      Reflections.getOrForceToNativeString(s) should be (Reflections.toNativeString(s))
    }
  }

}
