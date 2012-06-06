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

import instrument.Instrumentation
import management.ManagementFactory
import com.github.zhongl.yascli.{PrintOut, Command}
import java.io.{BufferedWriter, FileWriter, File}
import java.lang.reflect.Method
import Reflections.allMethodsOf

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Trace(val inst: Instrumentation, out: PrintOut)
  extends Command("trace", "display or output infomation of method invocaton.", out) with Transformer {

  val outputRoot = {
    val dir = new File("/tmp/" + name + "/" + ManagementFactory.getRuntimeMXBean.getName)
    dir.mkdirs()
    dir
  }
  val detailFile = new File(outputRoot, "detail")
  val stackFile  = new File(outputRoot, "stack")

  private val interval   = option[Second]("-i" :: "--interval" :: Nil, "display trace statistics interval.", 1)
  private val detailable = flag("-d" :: "--detail" :: Nil, "enable append invocation detail to " + detailFile + ".")
  private val stackable  = flag("-s" :: "--stack" :: Nil, "enable append invocation calling stack to " + stackFile + ".")

  override protected def hook = new Hook() {
    val enableDetail   = detailable()
    val enableStack    = stackable()
    var last           = System.currentTimeMillis()
    val intervalMillis = interval().toMillis
    val statistics     = {
      val mfs = methodFilters()
      for (c <- candidates; m <- allMethodsOf(c) if mfs.find(_.filter(c, m)).isDefined) yield new Statistic(c, m)
    }.sortBy(s => s.klass.getName + "." + s.method.getName)

    lazy val detailWriter = new DetailWriter(new BufferedWriter(new FileWriter(detailFile, true)))
    lazy val stackWriter  = new StackWriter(new BufferedWriter(new FileWriter(stackFile, true)))

    override def exitWith(context: Context) {
      if (enableDetail) detailWriter.write(context)
      if (enableStack) stackWriter.write(context)
      statistics find { s =>
        context.className == s.method.getDeclaringClass.getName && context.methodName == s.method.getName
      } match {
        case Some(s) => s + context
        case None    => // ignore
      }
    }

    override def heartbeat(now: Long) {
      if (now - last >= intervalMillis) {
        last = now
        statistics foreach println
        println()
      }
    }

    override def end() {
      if (enableDetail) detailWriter.close()
      if (enableStack) stackWriter.close()
    }
  }

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

    def x(context: Context) = {
      context.thisObject.getClass == klass &&
        context.methodName == method.getName &&
        context.arguments.size == method.getParameterTypes.size &&
        context.arguments.map(_.getClass) == method.getParameterTypes
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


