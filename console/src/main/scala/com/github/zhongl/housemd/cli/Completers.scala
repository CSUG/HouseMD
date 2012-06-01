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

package com.github.zhongl.housemd.cli

import collection.JavaConversions._
import jline.console.completer.Completer
import java.util.SortedSet

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

trait CommandCompleter extends Completer {self: Commands =>
  private val RE0 = """\s+""".r
  private val RE1 = """\s*(\w+)""".r
  private val RE2 = """\s*(\w+)(.+)""".r

  override def complete(buffer: String, cursor: Int, candidates: java.util.List[CharSequence]): Int = buffer match {
    case null | RE0()    => candidates.addAll(commands); 0
    case RE1(part)       => commands.tailSet(part) filter {_.startsWith(part)} foreach {candidates.add}; 0
    case RE2(name, part) => command(name) match {
      case None          => warn("Invalid command {}", name); -1
      case Some(command) => command.complete(part, cursor, candidates)
    }
  }
}

trait FirstArgumentCompleter extends Completer {
  protected def allCandidates:SortedSet[String]

  override def complete(buffer: String, cursor: Int, candidates: java.util.List[CharSequence]) = {
    val trimmed = buffer.trim
    trimmed match {
      case "" => candidates.addAll(allCandidates); cursor
      case _  =>
        allCandidates.tailSet(trimmed) filter {_.startsWith(trimmed)} foreach {candidates.add}
        if (candidates.isEmpty) -1 else cursor - trimmed.size
    }
  }

}