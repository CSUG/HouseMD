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

import java.util.SortedSet
import com.github.zhongl.house.{Reflections, Utils, cli}
import Reflections._
import cli._

@command(name = "loaded", description = "output loaded classes information")
class Loaded(output: Output, vm: VirtualMachine) extends FirstArgumentCompleter {

  private[this] val tab = "\t"

  def apply(
    @argument(name = "name", description = "specical class name, eg: String@java.lang")
    name: String /*,
    @option(name = Array("-h", "--loader-hierarchies"), description = "show loader hierarchies of loaded classes")
    loaderHierarchies: Boolean = false*/) {

    implicit val o = output
    vm.allLoadedClasses filter {classNameOf(_).equals(name)} foreach {
      c =>
        output.println(c.getName + originOf(c))
      //        if (loaderHierarchies) layout(c.getClassLoader)
    }
  }

  protected def allCandidates = sortedSet(vm.allLoadedClasses.map { c => classNameOf(c) })

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

  private[this] def classNameOf(c: Class[_]): String = c.getSimpleName + "@" + c.getPackage.getName

  private def sortedSet[T](list: Array[T]): SortedSet[T] = {
    import collection.JavaConversions.asJavaCollection
    new java.util.TreeSet[T](list.toIterable)
  }
}


