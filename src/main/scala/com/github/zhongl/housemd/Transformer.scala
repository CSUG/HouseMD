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

import java.lang.System.{currentTimeMillis => now}
import java.util.regex.Pattern
import instrument.{ClassFileTransformer, Instrumentation}
import java.security.ProtectionDomain
import java.util.concurrent.atomic.AtomicInteger
import java.util.Map
import Reflections._
import com.github.zhongl.yascli.Command
import actors.Actor._
import actors.TIMEOUT
import java.lang.reflect.Modifier

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
trait Transformer extends Runnable {this: Command =>

  import com.github.zhongl.yascli.Converters._

  val inst: Instrumentation

  protected val packagePattern = option[Pattern]("-p" :: "--package" :: Nil, "package regex pattern for filtering.", ".*")
  protected val timeout        = option[Second]("-t" :: "--timeout" :: Nil, "limited trace seconds.", 10)
  protected val limit          = option[Int]("-l" :: "--limit" :: Nil, "limited limited times.", 1000)

  protected val methodFilters = parameter[Array[MethodFilter]]("method-filter", "method filter pattern like \"ClassSimpleName.methodName\" or \"ClassSimpleName\".")

  protected lazy val advice = new Advice() {
    val count = new AtomicInteger()
    val max   = limit()
    val host  = self

    def enterWith(context: Map[String, AnyRef]) {
      host ! EnterWith(context)
    }

    def exitWith(context: Map[String, AnyRef]) {
      host ! ExitWith(context)
      if (count.incrementAndGet() >= max) host ! OverLimit
    }
  }

  protected lazy val candidates = {
    val pp = packagePattern()
    val mfs = methodFilters()
    def packageOf(c: Class[_]): String = if (c.getPackage == null) "" else c.getPackage.getName

    inst.getAllLoadedClasses filter { c =>
      pp.matcher(packageOf(c)).matches() && mfs.find(_.filter(c)).isDefined && isNotFinal(c)
    }
  }

  protected lazy val probeTransformer = new ClassFileTransformer {

    def transform(
      loader: ClassLoader,
      className: String,
      classBeingRedefined: Class[_],
      protectionDomain: ProtectionDomain,
      classfileBuffer: Array[Byte]) = {
      if (candidates.contains(classBeingRedefined)) ClassDecorator.decorate(classfileBuffer, methodFilters()) else null
    }
  }

  override def run() {
    if (candidates.isEmpty) {println("No matched class"); return}
    probe()
    act()
    reset()
  }

  def cancel() { advice.host ! Cancel }

  protected def hook: Hook

  private def probe() {
    inst.addTransformer(probeTransformer, true)
    retransform("probe ")(_.getMethod(Advice.SET_DELEGATE, classOf[Object]).invoke(null, advice))
    inst.removeTransformer(probeTransformer)
  }

  private def act() {
    var cond = true
    val start = now
    val timoutMillis = timeout().toMillis
    val h = hook

    while (cond) {
      receiveWithin(500) {
        case TIMEOUT            => // ignore
        case OverLimit          => cond = false; info("Ended by overlimit")
        case Cancel             => cond = false; info("Ended by cancel.")
        case EnterWith(context) => h.enterWith(context)
        case ExitWith(context)  => h.exitWith(context)
        case x                  => error("Unknown case: " + x)
      }

      val t = now
      h.heartbeat(t)

      if (t - start >= timoutMillis) {
        cond = false
        info("Ended by timeout")
      }
    }

    h.end()
  }

  private def reset() {
    retransform("reset ")(_.getMethod(Advice.SET_DEFAULT_DELEGATE).invoke(null))
  }

  private def retransform(action: String)(handle: Class[_] => Unit) {
    candidates foreach { c =>
      try {
        handle(loadOrDefine(classOf[Advice], c.getClassLoader))
        inst.retransformClasses(c)
        info(action + c)
      } catch {
        case t: Throwable => warn("Failed to " + action + c + " because of " + t)
      }
    }
  }

  private def isNotFinal(c: Class[_]) = if (Modifier.isFinal(c.getModifiers)) {
    warn("Can't trace " + c + ", because it is final")
    false
  } else true

  sealed trait Event

  private case object OverLimit extends Event

  private case object Cancel extends Event

  case class EnterWith(context: Context) extends Event

  case class ExitWith(context: Context) extends Event

  trait Hook {
    def enterWith(context: Context) {}

    def exitWith(context: Context) {}

    def heartbeat(now: Long) {}

    def end() {}
  }

}
