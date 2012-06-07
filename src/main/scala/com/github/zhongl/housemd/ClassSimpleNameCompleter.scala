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

import jline.console.completer.Completer
import java.util.List
import com.github.zhongl.housemd.Reflections._
import instrument.Instrumentation
import java.lang.reflect.Method

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object ClassSimpleName {
  def unapply(c: Class[_]) = Some(simpleNameOf(c))
}

object MethodName {
  def unapply(m: Method) = Some(m.getName)
}

trait ClassSimpleNameCompleter extends Completer {
  val inst: Instrumentation

  def complete(buffer: String, cursor: Int, candidates: List[CharSequence]) = buffer.split("\\s+") match {
    case Array() => -1
    case all     => completeClassSimpleName(all.last, cursor, candidates)
  }

  protected def completeClassSimpleName(prefix: String, cursor: Int, candidates: List[CharSequence]): Int =
    inst.getAllLoadedClasses collect {case ClassSimpleName(c) if c.startsWith(prefix) => c } match {
      case Array() => -1
      case all     => all.distinct.sorted foreach {candidates.add}; cursor - prefix.length
    }
}

trait MethodFilterCompleter extends ClassSimpleNameCompleter {

  override protected def completeClassSimpleName(buffer: String, cursor: Int, candidates: List[CharSequence]) =
    buffer.split("\\.") match {
      case Array(prefix)                        => super.completeClassSimpleName(buffer, cursor, candidates)
      case Array(classSimpleName, methodPrefix) => complete(classSimpleName, methodPrefix, cursor, candidates)
      case _                                    => -1
    }

  private def complete(classSimpleName: String, methodPrefix: String, cursor: Int, candidates: List[CharSequence]) =
    inst.getAllLoadedClasses collect {
      case c@ClassSimpleName(n) if n == classSimpleName =>
        (c.getMethods ++ c.getDeclaredMethods)
          .collect { case MethodName(m) if m.startsWith(methodPrefix) => m }
    } match {
      case Array() => -1
      case all     => all.flatten.distinct.sorted foreach {candidates.add}; cursor - methodPrefix.length
    }
}

