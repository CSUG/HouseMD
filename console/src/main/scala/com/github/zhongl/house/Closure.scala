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

package com.github.zhongl.house

import actors.Actor
import management.ManagementFactory
import instrument.Instrumentation

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
trait Closure {
  def execute(): Unit
}

class Summary(console: Actor) extends Closure {
  def execute() {
    val runtime = ManagementFactory.getRuntimeMXBean
    console ! "name : " + runtime.getName
  }
}

class Enviroment(console: Actor, pattern: String = ".+") extends Closure {
  def execute() {
      for ((k, v) <- sys.env) {
        val line = k + " = " + v
        if (line.matches(pattern)) console ! line
      }
  }
}

class LoadedClasses(console: Actor,
                    inst: Instrumentation,
                    regex: String = ".+",
                    origin: Boolean = true,
                    loaderHierarchies: Boolean = false) extends Closure {
  def execute() {
    inst.getAllLoadedClasses.filter(_.getName.matches(regex)).foreach {
      c =>
        val from = if (origin) originOf(c) else ""
        console ! c.getName + from
        if (loaderHierarchies) layout(c.getClassLoader)
    }
  }

  private[this] def layout(cl: ClassLoader, t: String = "- ") {
    if (cl != null) {
      console ! '\t' + t + cl.getClass.getName + '@' + System.identityHashCode(cl)
      layout(cl.getParent, '\t' + t)
    }
  }

  private[this] def originOf(c: Class[_]): String = " -> " + c.getResource("/" + c.getName.replace('.', '/') + ".class").toString
}

class Trace(console: Actor, inst: Instrumentation, regex: String) extends Closure {
  def execute() {


  }
}