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
import com.github.zhongl.test.A

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class ReflectionUtilsSpec extends FunSpec with ShouldMatchers {
  describe("ReflectionUtils") {
    it("should define Advice") {
      ReflectionUtils.loadOrDefine(classOf[Advice], new ClassLoader() {
        override def loadClass(name: String) = {
          if (name == classOf[Advice].getName) throw new ClassNotFoundException()
          else super.loadClass(name)
        }
      })
    }

    it("shoulde get object native string") {
      val o = new Object()
      ReflectionUtils.toNativeString(o) should be(o.toString)
    }

    it("shoulde get or force to native string") {
      val o = new Object()
      ReflectionUtils.getOrForceToNativeString(o) should be(o.toString)
    }

    it("should force to native string") {
      val s = "hello"
      ReflectionUtils.getOrForceToNativeString(s) should be(ReflectionUtils.toNativeString(s))
    }

    it("should include <init> if it is a class") {
      ReflectionUtils.constructorAndMethodNamesOf(classOf[A]) should contain ("<init>")
    }

    it("should not include <init> if it is a interface") {
      ReflectionUtils.constructorAndMethodNamesOf(classOf[Runnable]) should not contain ("<init>")
    }
  }

}
