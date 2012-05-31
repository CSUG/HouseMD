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

import java.util.concurrent.TimeUnit._
import java.lang.System.{currentTimeMillis => now}
import java.util.Date
import com.github.zhongl.command.{PrintOut, Command}
import java.util.regex.Pattern
import java.lang.reflect.Modifier
import instrument.{ClassFileTransformer, Instrumentation}
import java.security.ProtectionDomain
import java.util.concurrent.{TimeUnit, LinkedBlockingQueue}
import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object Trace extends AbstractIterator[String] with HaltAdvice {

  def enterWith(context: Context) {}

  def exitWith(context: Context) {
    val started = "%1$tF %1$tT" format (new Date(context.startAt))
    val elapse = "%,dms" format (context.stopAt - context.startAt)
    val thread = Thread.currentThread().getName
    val method = context.className + "." + context.methodName
    val arguments = context.arguments.mkString(" ")
    val resultOrExcption = context.resultOrException match {
      case null => "null"
      case x    => x.toString
    }
    queue.offer(Array(started, elapse, thread, method, arguments, resultOrExcption).mkString(" "))
  }

  def halt() { isHalt.set(true) }

  protected def computeNext(): Option[String] = {
    if (isHalt.get()) {
      None
    } else {
      val next = queue.poll(500L, MILLISECONDS)
      if (next == null) computeNext() else Some(next)
    }
  }

  private[this] lazy val queue = new LinkedBlockingQueue[String]

  private[this] lazy val isHalt = new AtomicBoolean(false)
}

import java.lang.System.{currentTimeMillis => now}
import actors.Actor._

class Trace(inst: Instrumentation, out: PrintOut) extends Command("trace", "trace method invocaton", out) {

  import com.github.zhongl.command.Converters._

  private implicit val int2Seconds    = new Seconds(_: Int)
  private implicit val string2Seconds = (s: String) => new Seconds(s)

  private val packagePattern = option[Pattern]("-p" :: "--package" :: Nil, "package regex pattern.", ".*")
  private val timeout        = option[Seconds]("-t" :: "--timeout" :: Nil, "limited trace seconds.", 10)
  private val limit          = option[Int]("-l" :: "--limit" :: Nil, "limited limited times.", 1000)

  private val classPattern  = parameter[Pattern]("class", "class name regex pattern.")
  private val methodPattern = parameter[Pattern]("method", "method name regex pattern.", Some(".+"))

  private lazy val probeTransformer = new ClassFileTransformer {
    def transform(
      loader: ClassLoader,
      className: String,
      classBeingRedefined: Class[_],
      protectionDomain: ProtectionDomain,
      classfileBuffer: Array[Byte]) = null
  }

  def run() {
    val pp = packagePattern()
    val cp = classPattern()

    val candidates = inst.getAllLoadedClasses filter { c =>
      isFinal(c) && pp.matcher(c.getPackage.getName).matches() && cp.matcher(c.getSimpleName).matches()
    }

    AdviceProxy.delegate = advice

    probe(candidates)

    waitForTimeoutOrOverLimit()

    reset(candidates)
  }

  def advice = new Advice {
    val count = new AtomicInteger()
    val max   = limit()
    val host  = self

    def enterWith(context: Context) {}

    def exitWith(context: Context) {
      val started = "%1$tF %1$tT" format (new Date(context.startAt))
      val elapse = "%,dms" format (context.stopAt - context.startAt)
      val thread = Thread.currentThread().getName
      val method = context.className + "." + context.methodName
      val arguments = context.arguments.mkString(" ")
      val resultOrExcption = context.resultOrException match {
        case null => "null"
        case x    => x.toString
      }
      info((started :: elapse :: thread :: method :: arguments :: resultOrExcption :: Nil).mkString(" "))
      if (count.incrementAndGet() >= max) host ! Stop
    }
  }

  def waitForTimeoutOrOverLimit() {
    val start = now
    val millis = this.timeout().toMillis
    def timeout = now - start >= millis
    var overLimit = false

    loopWhile(timeout || overLimit) {
      reactWithin(100) { case Stop => overLimit = true }
    }
  }

  private def probe(candidates: Array[Class[_]]) {
    inst.addTransformer(probeTransformer, true)
    retransform(candidates, "probe ")
    inst.removeTransformer(probeTransformer)
  }

  private def reset(candidates: Array[Class[_]]) {
    retransform(candidates, "reset ")
  }

  def retransform(candidates: Array[Class[_]], action: String) {
    candidates foreach { c => inst.retransformClasses(c); info(action + c) }
  }

  private def isFinal(c: Class[_]) = if (Modifier.isFinal(c.getModifiers)) {
    warn("Can't trace " + c + ", because it is final."); false
  } else true

  class Seconds(val value: Int) {
    def toMillis = TimeUnit.SECONDS.toMillis(value)
  }

  case class Stop()

}


