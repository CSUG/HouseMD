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

import java.lang.reflect.Field
import java.util.List
import com.github.zhongl.housemd.misc.ReflectionUtils._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

trait ClassMemberCompleter extends ClassSimpleNameCompleter {

  override protected def completeClassSimpleName(buffer: String, cursor: Int, candidates: List[CharSequence]) =
    buffer.split("\\.") match {
      case Array(classSimpleName) if buffer.endsWith(".") => completeAll(classSimpleName, cursor, candidates)
      case Array(prefix)                                  => super.completeClassSimpleName(buffer, cursor, candidates)
      case Array(classSimpleName, prefix)                 => complete(classSimpleName, prefix, cursor, candidates)
      case _                                              => -1
    }

  override protected def collectLoadedClassNames(prefix: String) = inst.getAllLoadedClasses collect {
    case c@ClassSimpleName(n) if n.startsWith(prefix) && isNotConcrete(c) => n + "+"
    case ClassSimpleName(n) if n.startsWith(prefix)                       => n
  }

  protected def completeAll(buffer: String, cursor: Int, candidates: List[CharSequence]): Int

  protected def complete(simpleName: String, member: String, cursor: Int, candidates: List[CharSequence]): Int

}

trait MethodFilterCompleter extends ClassMemberCompleter {

  private def allDeclaredMethodsOf(classSimpleName: String)(collect: Array[String] => Array[String]) =
    inst.getAllLoadedClasses collect {
      case c@ClassSimpleName(n) if (n == classSimpleName || n + "+" == classSimpleName) => collect(constructorAndMethodNamesOf(c))
    }

  override protected def completeAll(classSimpleName: String, cursor: Int, candidates: List[CharSequence]) =
    allDeclaredMethodsOf(classSimpleName) {a => a} match {
      case Array() => -1
      case all     => all.flatten.sorted foreach {candidates.add}; cursor
    }

  override protected def complete(simpleName: String, prefix: String, cursor: Int, candidates: List[CharSequence]) =
    allDeclaredMethodsOf(simpleName) {_ collect {case m if m.startsWith(prefix) => m }} match {
      case Array() => -1
      case all     => all.flatten.sorted foreach {candidates.add}; cursor - prefix.length
    }
}

// FIXME: reduce duplication in MemberFilterCompleter
trait FieldFilterCompleter extends ClassMemberCompleter {

  private def allDeclaredFieldsOf(classSimpleName: String)(collect: Array[Field] => Array[String]) =
    inst.getAllLoadedClasses collect {
      case c@ClassSimpleName(n) if (n == classSimpleName || n + "+" == classSimpleName) => collect(c.getDeclaredFields)
    }

  override protected def completeAll(classSimpleName: String, cursor: Int, candidates: List[CharSequence]) =
    allDeclaredFieldsOf(classSimpleName) {_ map {_.getName}} match {
      case Array() => -1
      case all     => all.flatten.sorted foreach {candidates.add}; cursor
    }

  override protected def complete(simpleName: String, prefix: String, cursor: Int, candidates: List[CharSequence]) =
    allDeclaredFieldsOf(simpleName) {_ collect {case f if f.getName.startsWith(prefix) => f.getName }} match {
      case Array() => -1
      case all     => all.flatten.sorted foreach {candidates.add}; cursor - prefix.length
    }
}