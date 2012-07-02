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
import java.security.ProtectionDomain
import java.util.concurrent.atomic.AtomicInteger
import actors.Actor._

import com.github.zhongl.housemd.misc.ReflectionUtils._
import com.github.zhongl.yascli.Loggable
import java.lang.System.{currentTimeMillis => now}
import actors.TIMEOUT
import java.util

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Transform extends ((Instrumentation, Filter, Seconds, Int, Loggable, Hook) => Unit) {

  def apply(inst: Instrumentation, filter: Filter, timeout: Seconds, overLimit: Int, log: Loggable, hook: Hook) {
    implicit val i = inst
    implicit val t = timeout
    implicit val l = log
    implicit val h = hook

    val candidates = inst.getAllLoadedClasses filter { c =>

      @inline
      def skipClass(description: String)(cause: Class[_] => Boolean) =
        if (cause(c)) {log.warn("Skip %1$s %2$s" format(c, description)); false} else true

      @inline
      def isNotBelongsHouseMD = skipClass("belongs to HouseMD") {_.getName.startsWith("com.github.zhongl.housemd")}

      @inline
      def isNotInterface = skipClass("") {_.isInterface}

      @inline
      def isNotFromBootClassLoader = skipClass("loaded from bootclassloader") {isFromBootClassLoader}

      filter(c) && isNotBelongsHouseMD && isNotInterface && isNotFromBootClassLoader
    }

    if (candidates.isEmpty) {
      log.println("No matched class")
    } else {
      val probeDecorator = classFileTransformer(filter, candidates)

      inst.addTransformer(probeDecorator, true)
      probe(candidates, advice(overLimit))

      try {handleAdviceEvent} finally {
        inst.removeTransformer(probeDecorator)
        reset(candidates)
      }

    }

  }

  private def handleAdviceEvent(implicit timeout: Seconds, log: Loggable, h: Hook) {
    val start = now
    val timoutMillis = timeout.toMillis

    try {
      while (true) {

        receiveWithin(500) {
          case TIMEOUT            => // ignore
          case OverLimit          => throw OverLimitBreak
          case EnterWith(context) => h.enterWith(context)
          case ExitWith(context)  => h.exitWith(context)
          case x                  => // ignore last unread messages, error("Unknown case: " + x)
        }

        val t = now
        h.heartbeat(t)
        if (t - start >= timoutMillis) throw TimeoutBreak
      }
    } catch {
      case OverLimitBreak => h.finalize(None); log.info("Ended by overlimit")
      case TimeoutBreak   => h.finalize(None); log.info("Ended by timeout")
      case t              => h.finalize(Some(t)); throw t
    }
  }

  private def classFileTransformer(filter: Filter, classes: Array[Class[_]])(implicit log: Loggable) =
    new ClassFileTransformer {
      def transform(loader: ClassLoader, name: String, klass: Class[_], pd: ProtectionDomain, bytecode: Array[Byte]) = {
        try {
          if (classes.contains(klass)) ClassDecorator.decorate(bytecode, name, filter.curried(klass)) else null
        } catch {
          case e => log.error(e); null
        }
      }
    }

  private def probe(classes: Array[Class[_]], advice: Advice)(implicit inst: Instrumentation, log: Loggable) {
    classes foreach { c =>
      try {
        loadOrDefineAdviceClassFrom(c.getClassLoader)
          .getMethod(Advice.SET_DELEGATE, classOf[Object])
          .invoke(null, advice)
        inst.retransformClasses(c)
        log.info("Probe " + c)
      } catch {
        case e => log.warn("Failed to probe " + c + " because of " + e)
      }
    }
  }

  // FIXME : reduce duplication between probe and reset .
  private def reset(classes: Array[Class[_]])(implicit inst: Instrumentation, log: Loggable) {
    classes foreach { c =>
      try {
        loadOrDefineAdviceClassFrom(c.getClassLoader).getMethod(Advice.SET_DEFAULT_DELEGATE).invoke(null)
        inst.retransformClasses(c)
        log.info("Reset " + c)
      } catch {
        case e => log.warn("Failed to reset " + c + " because of " + e)
      }
    }
  }


  private def loadOrDefineAdviceClassFrom(loader: ClassLoader): Class[_] = loadOrDefine(classOf[Advice], loader)

  private def advice(limit: Int) = new Advice() {

    val count = new AtomicInteger()
    val host  = self

    def enterWith(context: util.Map[String, AnyRef]) {
      host ! EnterWith(context)
    }

    def exitWith(context: util.Map[String, AnyRef]) {
      host ! ExitWith(context)
      if (count.incrementAndGet() >= limit) host ! OverLimit
    }
  }

  private sealed trait Event

  private case object OverLimit extends Event

  private case class EnterWith(context: Context) extends Event

  private case class ExitWith(context: Context) extends Event

  private object OverLimitBreak extends Exception

  private object TimeoutBreak extends Exception

}

