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

import java.lang.System.{currentTimeMillis => now}
import java.io.FileNotFoundException
import scala.Predef._
import scala.Array
import instrument.{ClassFileTransformer, Instrumentation}
import java.security.ProtectionDomain
import java.lang.reflect.Modifier

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Transformer(inst: Instrumentation, methodRegexs: Traversable[String], agentJarPath: String) {

  private[this] lazy val toProbeClasses = inst.getAllLoadedClasses filter { c =>
    (!Modifier.isFinal(c.getModifiers)) && (c.getDeclaredMethods ++ c.getMethods).find { m =>
      methodRegexs.find((c.getName + "." + m.getName).matches).isDefined
    }.isDefined
  }

  private[this] lazy val probeTransformer = classFileTransformer {
    (loader: ClassLoader, className: String, classfileBuffer: Array[Byte]) =>
    // TODO LOGGER.info(format("probe class {1} from {0}", loader, className))
      ClassDecorator.decorate(classfileBuffer, methodRegexs)
  }

  private[this] lazy val resetTransformer = classFileTransformer {
    (loader: ClassLoader, className: String, classfileBuffer: Array[Byte]) =>
    // TODO log "reset class {1} from {0}", loader, className
      getOriginClassFileBytes(loader, className)
  }

  def probe() {
    transformBy(probeTransformer)
  }

  def reset() {
    transformBy(resetTransformer)
  }

  private[this] def getOriginClassFileBytes(loader: ClassLoader, className: String): Array[Byte] = {
    val stream = loader.getResourceAsStream(className + ".class")
    if (stream == null) throw new FileNotFoundException
    Utils.toBytes(stream)
  }

  private[this] def transformBy(t: ClassFileTransformer) {
    val classes = toProbeClasses.toArray // eval first for avoiding ClassCircularityError cause be retransforming.
    inst.addTransformer(t, true)
    try {
      inst.retransformClasses(classes: _*)
    } finally {
      inst.removeTransformer(t)
    }
  }

  private[this] def classFileTransformer(fun: (ClassLoader, String, Array[Byte]) => Array[Byte]) =
    new ClassFileTransformer {
      def transform(
        loader: ClassLoader,
        className: String,
        classBeingRedefined: Class[_],
        protectionDomain: ProtectionDomain,
        classfileBuffer: Array[Byte]) = {
        var bytes = classfileBuffer
        try {
          toProbeClasses.find {_.getName.replace('.', '/') == className} match {
            case Some(_) => bytes = fun(loader, className, classfileBuffer)
            case None    => // ignore
          }
        } catch {
          case e =>
            e.printStackTrace()
          // TODO log "transfor but not reset class {1} from {0}", loader, className
        }
        bytes
      }
    }
}

