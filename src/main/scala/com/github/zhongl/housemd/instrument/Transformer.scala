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

package com.github.zhongl.housemd.instrument

import java.lang.instrument.{ClassFileTransformer, Instrumentation}
import com.github.zhongl.yascli.Loggable
import java.security.ProtectionDomain

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Transformer extends ((Instrumentation, Filter, Seconds, Long, Loggable) => Unit) {

  def apply(implicit inst: Instrumentation, filter: Filter, timeout: Seconds, overLimit: Long, log: Loggable) {
    val candidates = inst.getAllLoadedClasses filter {filter}

    if (candidates.isEmpty) {
      log.println("No matched class")
    } else {
      val probeDecorator = classFileTransformer(filter, candidates)
      inst.addTransformer(probeDecorator, true)
      probe(candidates)

      inst.removeTransformer(probeDecorator)
    }

  }

  private def classFileTransformer(filter: Filter, classes: Array[Class[_]])(implicit log: Loggable) =
    new ClassFileTransformer {
      def transform(loader: ClassLoader, name: String, klass: Class[_], pd: ProtectionDomain, bytecode: Array[Byte]) = {
        try {
          if (classes.contains(klass)) {
            ClassDecorator.decorate(bytecode, name, filter.curried(klass))
          } else null
        } catch {
          case e => log.error(e); e.getStackTrace.foreach { s => log.println("\t" + s) }; null
        }
      }
    }

  private def probe(classes: Array[Class[_]])(implicit inst: Instrumentation) {

  }
}


class Filter extends (Class[_] => Boolean) with ((Class[_], String) => Boolean) {
  def apply(c: Class[_]) = false

  def apply(c: Class[_], m: String) = false
}