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
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.Map
import management.ManagementFactory
import com.github.zhongl.yascli.{PrintOut, Command}
import actors.Actor._
import java.io.{BufferedWriter, FileWriter, File}
import actors.TIMEOUT
import java.lang.reflect.{Method, Modifier}


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Trace(inst: Instrumentation, out: PrintOut) extends Command("trace", "trace method invocaton.", out) {

  import com.github.zhongl.yascli.Converters._

  private implicit val int2Seconds              = new Second(_: Int)
  private implicit val string2Seconds           = (s: String) => new Second(s)
  private implicit val arrayString2ArrayPattern = (_: String).split("\\s+") map (Pattern.compile)

  val outputRoot = {
    val dir = new File("/tmp/" + name + "/" + ManagementFactory.getRuntimeMXBean.getName)
    dir.mkdirs()
    dir
  }
  val detailFile = new File(outputRoot, "detail")
  val stackFile  = new File(outputRoot, "stack")

  private val packagePattern = option[Pattern]("-p" :: "--package" :: Nil, "package regex pattern for filtering.", ".*")
  private val timeout        = option[Second]("-t" :: "--timeout" :: Nil, "limited trace seconds.", 10)
  private val limit          = option[Int]("-l" :: "--limit" :: Nil, "limited limited times.", 1000)
  private val interval       = option[Second]("-i" :: "--interval" :: Nil, "display trace statistics interval.", 1)
  private val detailable     = flag("-d" :: "--detail" :: Nil, "enable append invocation detail to " + detailFile + ".")
  private val stackable      = flag("-s" :: "--stack" :: Nil, "enable append invocation calling stack to " + stackFile + ".")

  private val classPattern   = parameter[Pattern]("class", "class name regex pattern.")
  private val methodPatterns = parameter[Array[Pattern]]("method", "method name regex pattern.", Some(Array[Pattern](".+")))

  private lazy val detailWriter = new DetailWriter(new BufferedWriter(new FileWriter(detailFile, true)))
  private lazy val stackWriter  = new StackWriter(new BufferedWriter(new FileWriter(stackFile, true)))

  private def candidates = {
    val pp = packagePattern()
    val cp = classPattern()
    def packageOf(c: Class[_]): String = if (c.getPackage == null) "" else c.getPackage.getName
    inst.getAllLoadedClasses filter { c =>
      try {
        pp.matcher(packageOf(c)).matches() && cp.matcher(c.getSimpleName).matches() && isNotFinal(c)
      } catch {
        case e: InternalError => false
      }
    }
  }

  def run() {
    val c = candidates
    if (c.isEmpty) {println("No matched class"); return}
    probe(c)
    waitForTimeoutOrOverLimitOrCancel()
    reset(c)
  }

  def cancel() { advice.host ! Cancel }

  private def probe(candidates: Array[Class[_]]) {
    val p = probeTransformer
    val a = advice
    inst.addTransformer(p, true)
    retransform(candidates, "probe ")(_.getMethod(Advice.SET_DELEGATE, classOf[Object]).invoke(null, a))
    inst.removeTransformer(p)
  }

  private def probeTransformer = new ClassFileTransformer {
    val patterns = methodPatterns()

    def transform(
      loader: ClassLoader,
      className: String,
      classBeingRedefined: Class[_],
      protectionDomain: ProtectionDomain,
      classfileBuffer: Array[Byte]) = {
      if (candidates.contains(classBeingRedefined)) ClassDecorator.decorate(classfileBuffer, patterns) else null
    }
  }

  private def advice = new Advice {
    val count = new AtomicInteger()
    val max   = limit()
    val host  = self

    def enterWith(context: Map[String, AnyRef]) {}

    def exitWith(context: Map[String, AnyRef]) {
      import Context.map2Context

      host ! HandleInvocation(context)
      if (count.incrementAndGet() >= max) host ! OverLimit
    }
  }

  private def waitForTimeoutOrOverLimitOrCancel() {
    val statistics = initializeStatistics.sortBy(s => s.klass.getName + "." + s.method.getName)

    var cond = true
    var last = now
    val start = last
    val timoutMillis = timeout().toMillis
    val intervalMillis = interval().toMillis
    val enableDetail = detailable()
    val enableStack = stackable()

    while (cond) {
      receiveWithin(500) {
        case OverLimit                 => cond = false; info("End tracing by overlimit")
        case Cancel                    => cond = false; info("End tracing by cancel.")
        case HandleInvocation(context) =>
          if (enableDetail) detailWriter.write(context)
          if (enableStack) stackWriter.write(context)
          statistics find { s =>
            context.className == s.method.getDeclaringClass.getName && context.methodName == s.method.getName
          } match {
            case Some(s) => s + context
            case None    => // ignore
          }
        case TIMEOUT                   => //ignore
        case x                         => error("Unknown case: " + x)
      }

      val t = now

      if (t - last >= intervalMillis) {
        last = t
        statistics foreach println
        println()
      }

      if (t - start >= timoutMillis) {
        cond = false
        info("End tracing by timeout")
      }
    }

    detailWriter.close()
    stackWriter.close()
  }

  private def initializeStatistics = {
    val mp = methodPatterns()
    for (
      c <- candidates;
      m <- (c.getMethods ++ c.getDeclaredMethods).toSet
      if mp.find(_.matcher(m.getName).matches()).isDefined
    ) yield new Statistic(c, m)
  }

  private def reset(candidates: Array[Class[_]]) {
    retransform(candidates, "reset ")(_.getMethod(Advice.SET_DEFAULT_DELEGATE).invoke(null))
  }

  private def retransform(candidates: Array[Class[_]], action: String)(handle: Class[_] => Unit) {
    import Reflections._
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

  class Second(val value: Int) {
    def toMillis = TimeUnit.SECONDS.toMillis(value)

    override def toString = value.toString
  }

  case class OverLimit()

  case class Cancel()

  case class HandleInvocation(context: Context)

  class Statistic(
    val klass: Class[_],
    val method: Method,
    var totalTimes: Long = 0,
    var failureTimes: Long = 0,
    var minElapseMillis: Long = -1,
    var maxElapseMillis: Long = -1,
    var totalElapseMills: Long = 0) {

    val NaN = "-"

    def +(context: Context) {
      import scala.math._
      val elapseMillis = context.stopped.get - context.started

      totalTimes = totalTimes + 1
      if (context.resultOrException.isInstanceOf[Throwable]) failureTimes = failureTimes + 1
      minElapseMillis = min(minElapseMillis, elapseMillis)
      maxElapseMillis = max(maxElapseMillis, elapseMillis)
      totalElapseMills = totalElapseMills + elapseMillis
    }

    def avgElapseMillis =
      if (totalTimes == 0) NaN else if (totalElapseMills < totalTimes) "<1" else totalElapseMills / totalTimes

    override def toString = "%1$-40s %2$#9s %3$#9s %4$#9s %5$#9s %6$#9s %7$s" format(
      "%1$s.%2$s".format(klass.getName.split("\\.").last, method.getName),
      totalTimes,
      failureTimes,
      (if (minElapseMillis == -1) NaN else minElapseMillis),
      (if (maxElapseMillis == -1) NaN else maxElapseMillis),
      avgElapseMillis,
      klass.getClassLoader)

  }

}


