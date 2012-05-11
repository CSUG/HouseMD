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

package com.github.zhongl.house.closures

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

import java.util.List
import instrument.Instrumentation
import com.github.zhongl.house.{Reflections, Utils, cli}
import Reflections._
import cli._
import jline.console.completer.Completer

@command(name = "loaded", description = "output loaded classes information")
class Loaded(output: String => Unit, instrumentation: Instrumentation) extends Completer {

  private[this] val tab = "\t"

  def apply(
    @argument(name = "regex", description = "regex for matching loaded classes")
    regex: String = ".+" /*,
    @option(name = Array("-h", "--loader-hierarchies"), description = "show loader hierarchies of loaded classes")
    loaderHierarchies: Boolean = false*/) {

    implicit val o = output
    instrumentation.getAllLoadedClasses filter {_.getName.matches(regex)} foreach {
      c =>
        output(c.getName + originOf(c))
      //        if (loaderHierarchies) layout(c.getClassLoader)
    }
  }

  private[this] def layout(cl: ClassLoader, lastIndents: String = "- ")(implicit output: String => Unit) {
    cl match {
      case null => Unit
      case _    =>
        val indents = tab + lastIndents
        output(indents + nativeToStringOf(cl))
        layout(cl.getParent, indents)
    }
  }

  private[this] def originOf(c: Class[_]): String = " -> " + Utils.sourceOf(c)

  def complete(buffer: String, cursor: Int, candidates: List[CharSequence]) = {
    import collection.JavaConversions._

    val trimmed = buffer.trim
    val set = new java.util.TreeSet[String](instrumentation.getAllLoadedClasses.map {_.getName}.toSet)
    trimmed match {
      case "" => candidates.addAll(set); cursor
      case _  => cursor - trimmed.length
    }
  }
}


