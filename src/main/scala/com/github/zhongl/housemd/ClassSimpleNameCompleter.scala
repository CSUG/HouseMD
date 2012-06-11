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

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object ClassSimpleName {
  def unapply(c: Class[_]) = Some(simpleNameOf(c))
}

trait ClassSimpleNameCompleter extends Completer {
  val inst: Instrumentation

  def complete(buffer: String, cursor: Int, candidates: List[CharSequence]) = buffer.split("\\s+") match {
    case Array() => -1
    case all     => completeClassSimpleName(all.last, cursor, candidates)
  }

  protected def completeClassSimpleName(prefix: String, cursor: Int, candidates: List[CharSequence]): Int =
    collectLoadedClassNames(prefix) match {
      case Array() => -1
      case all     => all.distinct.sorted foreach {candidates.add}; cursor - prefix.length
    }

  protected def collectLoadedClassNames(prefix:String) = inst.getAllLoadedClasses collect {
    case ClassSimpleName(n) if n.startsWith(prefix) => n
  }
}


