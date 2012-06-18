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

import java.lang.reflect.Method
import com.github.zhongl.housemd.misc.Reflections._

trait MethodFilterCompleter extends ClassSimpleNameCompleter {

  import java.util.List

  object MethodName {
    def unapply(m: Method) = Some(m.getName)
  }

  override protected def completeClassSimpleName(buffer: String, cursor: Int, candidates: List[CharSequence]) =
    buffer.split("\\.") match {
      case Array(classSimpleName) if buffer.endsWith(".") => completeAll(classSimpleName, cursor, candidates)
      case Array(prefix)                                  => super.completeClassSimpleName(buffer, cursor, candidates)
      case Array(classSimpleName, methodPrefix)           => complete(classSimpleName, methodPrefix, cursor, candidates)
      case _                                              => -1
    }

  override protected def collectLoadedClassNames(prefix: String) = inst.getAllLoadedClasses collect {
    case c@ClassSimpleName(n) if n.startsWith(prefix) && isNotConcrete(c) => n + "+"
    case ClassSimpleName(n) if n.startsWith(prefix)                       => n
  }

  private def allDeclaredMethodsOf(classSimpleName: String)(collect: Array[Method] => Array[String]) =
    inst.getAllLoadedClasses collect {
      case c@ClassSimpleName(n) if (n == classSimpleName || n + "+" == classSimpleName) => collect(c.getDeclaredMethods)
    }

  private def completeAll(classSimpleName: String, cursor: Int, candidates: List[CharSequence]) =
    allDeclaredMethodsOf(classSimpleName) {_ map {_.getName}} match {
      case Array() => -1
      case all     => all.flatten.sorted foreach {candidates.add}; cursor
    }

  private def complete(classSimpleName: String, methodPrefix: String, cursor: Int, candidates: List[CharSequence]) =
    allDeclaredMethodsOf(classSimpleName) {_ collect {case MethodName(m) if m.startsWith(methodPrefix) => m }} match {
      case Array() => -1
      case all     => all.flatten.sorted foreach {candidates.add}; cursor - methodPrefix.length
    }
}