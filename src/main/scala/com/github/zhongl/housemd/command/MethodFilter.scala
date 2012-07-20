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

package com.github.zhongl.housemd.command

import com.github.zhongl.housemd.misc.ReflectionUtils._
import java.lang.reflect.Type


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class MethodFilter(classSimpleName: String, methodName: String = "*") {
  implicit val type2Class = (_: Type).asInstanceOf[Class[_]]

  def filter(c: Class[_], methodName: String): Boolean = lazyFilter(c)(methodName)

  def filter(c: Class[_]): Boolean = {
    if (!filterOnly(c)) false
    else {
      val filter = lazyFilter(true)(_)
      constructorAndMethodNamesOf(c).find { m => filter(m) }.isDefined
    }
  }

  private def lazyFilter(cond: => Boolean)(m: String) = if (cond) {methodName == "*" || methodName == m} else false

  private def lazyFilter(c: Class[_]): (String => Boolean) = lazyFilter(filterOnly(c))

  private def filterOnly(className: String, superClassName: String, interfaceNames: Array[String]): Boolean = {
    if (classSimpleName.endsWith("+")) {
      val realname = classSimpleName.dropRight(1)
      (simpleNameOf(className) == realname ||
        superClassName != null && simpleNameOf(superClassName) == realname) ||
        interfaceNames.find(simpleNameOf(_) == realname).isDefined
    } else {
      simpleNameOf(className) == classSimpleName
    }
  }

  private def filterOnly(c: Class[_]): Boolean = {
    val superClassName = if (c.getSuperclass == null) null else c.getSuperclass.getName
    val interfaceNames = c.getInterfaces.map(_.getName)
    filterOnly(c.getName, superClassName, interfaceNames)
  }

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


