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


import instrument.Instrumentation
import scala.annotation.tailrec
import com.github.zhongl.yascli.{Command, PrintOut}
import Reflections._
import collection.immutable.SortedSet
import jline.console.completer.Completer

import collection.breakOut


class Loaded(inst: Instrumentation, out: PrintOut)
  extends Command("loaded", "output loaded classes information.", out)
          with Completer {

  private val tab = "\t"

  private val hierarchyable = flag("-h" :: "--classloader-hierarchies" :: Nil, "display classloader hierarchies of loaded class.")
  private val className     = parameter[String]("classname", "specical class name, eg: String@java.lang")

  override def run() {
    inst.getAllLoadedClasses find {classNameOf(_) == className()} match {
      case Some(c) => println(c.getName + originOf(c)); if (hierarchyable()) layout(c.getClassLoader)
      case None    => println("No matched class")
    }
  }

  override def complete(buffer: String, cursor: Int, candidates: java.util.List[CharSequence]) = {
    val trimmed = buffer.trim
    if (trimmed.isEmpty) {
      import collection.JavaConversions._

      candidates.addAll(allCandidates)
      cursor
    } else {
      allCandidates.from(trimmed) filter {_.startsWith(trimmed)} foreach {candidates.add}
      if (candidates.isEmpty) -1 else cursor - trimmed.size
    }
  }

  private def allCandidates: SortedSet[String] = inst.getAllLoadedClasses.map(classNameOf)(breakOut)

  @tailrec
  private def layout(cl: ClassLoader, lastIndents: String = "- ") {
    cl match {
      case null => Unit
      case _    =>
        val indents = tab + lastIndents
        println(indents + nativeToStringOf(cl))
        layout(cl.getParent, indents)
    }
  }

  private def originOf(c: Class[_]): String = " -> " + Utils.sourceOf(c)

  private def classNameOf(c: Class[_]): String = c.getSimpleName + "@" + c.getPackage.getName
}


