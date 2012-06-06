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

package com.github.zhongl.housemd

import Reflections._
import java.lang.reflect.Method

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class MethodFilter(classSimpleName: String, methodName: String = "*") {
  def filter(c: Class[_]) = simpleNameOf(c) == classSimpleName

  def filter(className: String, methodName: String) =
    simpleNameOf(className) == classSimpleName && this.methodName == "*" && methodName == this.methodName

  def filter(c: Class[_], m: Method): Boolean = filter(c.getName, m.getName)

}

object MethodFilter {

  implicit def apply(s: String) = {
    s.split("\\.") match {
      case Array(classSimpleName, methodName) => new MethodFilter(classSimpleName, methodName)
      case Array(classSimpleName)             => new MethodFilter(classSimpleName)
      case _                                  =>
        throw new IllegalArgumentException(", it should be \"ClassSimpleName.methodName\" or \"ClassSimpleName\"")
    }
  }

  implicit val string2MethodFilters = (_: String).split("\\s+") map {apply}
}
