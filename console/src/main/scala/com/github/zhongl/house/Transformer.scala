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
import java.lang.reflect.Method
import java.io.FileNotFoundException
import scala.Predef._
import scala.Array
import instrument.{ClassDefinition, ClassFileTransformer, Instrumentation}
import java.security.ProtectionDomain

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Transformer(inst: Instrumentation, methodRegexs: Traversable[String], agentJarPath:String) {

  private[this] lazy val toProbeClasses = inst.getAllLoadedClasses.filter(toProbe).toSet

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

  private[this] lazy val log = classFileTransformer {
    (loader: ClassLoader, className: String, classfileBuffer: Array[Byte]) =>
      classfileBuffer
  }

  def probe() {
    inst.addTransformer(log)
    redefine {
      c: Class[_] =>
        val loader: ClassLoader = c.getClassLoader
//        if (loader.isInstanceOf[URLClassLoader]) {
//          import java.net.URL
//          import java.io.File
//
//          AccessController.doPrivileged(new PrivilegedAction[Unit] {
//            def run() {
//              val method = loader.getClass.getDeclaredMethod("addURL", classOf[URL])
//              method.invoke(loader, new File(agentJarPath).toURI.toURL)
//            }
//          })
//        }
        val origin = getOriginClassFileBytes(loader, c.getName.replace('.', '/'))
        val decorated = ClassDecorator.decorate(origin, methodRegexs)
        new ClassDefinition(c, decorated)
    }
  }

  def reset() {
    redefine {
      c: Class[_] =>
        val origin = getOriginClassFileBytes(c.getClassLoader, c.getName.replace('.', '/'))
        new ClassDefinition(c, origin)
    }
    inst.removeTransformer(log)
  }

  private[this] def redefine(fun: Class[_] => ClassDefinition) {
    inst.redefineClasses(toProbeClasses.toArray.map {fun}: _*)
  }

  private[this] def getOriginClassFileBytes(loader: ClassLoader, className: String): Array[Byte] = {
    val stream = loader.getResourceAsStream(className + ".class")
    if (stream == null) throw new FileNotFoundException
    Utils.toBytes(stream)
  }

  private[this] def transformBy(t: ClassFileTransformer) {
    inst.addTransformer(t)
    try {
      inst.retransformClasses(toProbeClasses.toArray: _*)
    } finally {
      inst.removeTransformer(t)
    }
  }

  private[this] def toProbe(method: Method) = {
    val fullName = method.getDeclaringClass.getName + "." + method.getName
    !methodRegexs.find(fullName.matches).isEmpty
  }

  private[this] def toProbe(klass: Class[_]): Boolean = {
    val methods = (klass.getDeclaredMethods ++ klass.getMethods).toSet
    !methods.find(toProbe).isEmpty
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
          if (toProbeClasses.contains(classBeingRedefined)) {
            // TODO clean this
            bytes = fun(loader, className, classfileBuffer)
          }
        } catch {
          case e =>
          // TODO log "transfor but not reset class {1} from {0}", loader, className
        }
        bytes
      }
    }
}

