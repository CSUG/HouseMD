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
import com.github.zhongl.housemd.Reflections._
import java.util.Date

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Trace(val inst: Instrumentation, out: PrintOut)
  extends Command("trace", "display or output infomation of method invocaton.", out)
          with Transformer with MethodFilterCompleter {

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
    var last = System.currentTimeMillis()

    val enableDetail   = detailable()
    val enableStack    = stackable()
    val intervalMillis = interval().toMillis
    val statistics     = {
      val mfs = methodFilters()
      for (c <- candidates; m <- c.getDeclaredMethods if mfs.find(_.filter(c, m)).isDefined) yield new Statistic(c, m)
    }.sortBy(s => s.klass.getName + "." + s.method.getName)

    val maxMethodSignLength  = statistics.map {_.methodSign.length}.max
    val maxClassLoaderLength = statistics.map {_.maxClassLoaderLength}.max

    lazy val detailWriter = new DetailWriter(new BufferedWriter(new FileWriter(detailFile, true)))
    lazy val stackWriter  = new StackWriter(new BufferedWriter(new FileWriter(stackFile, true)))

    override def exitWith(context: Context) {
      if (enableDetail) detailWriter.write(context)
      if (enableStack) stackWriter.write(context)
      statistics find {_.filter(context)} match {
        case Some(s) => s + context
        case None    => // ignore
      }
    }

    override def heartbeat(now: Long) {
      if (now - last >= intervalMillis) {
        last = now
        statistics foreach { s => println(s.reps(maxMethodSignLength, maxClassLoaderLength)) }
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
    var thisObject: AnyRef = null,
    var totalTimes: Long = 0,
    var totalElapseMills: Long = 0) {

    val NaN = "-"

    lazy val methodSign = "%1$s.%2$s(%3$s)"
      .format(klass.getName.split("\\.").last, method.getName,
      method.getParameterTypes.map(simpleNameOf).mkString(", "))

    lazy val maxClassLoaderLength = {
      val loader = klass.getClassLoader
      if (loader == null) 4 else loader.toString.length
    }

    def +(context: Context) {
      val elapseMillis = context.stopped.get - context.started

      totalTimes = totalTimes + 1
      thisObject = context.thisObject
      totalElapseMills = totalElapseMills + elapseMillis
    }

    def filter(context: Context) = {
      context.classEquals(klass) && context.methodEquals(method)
    }

    def avgElapseMillis =
      if (totalTimes == 0) NaN else if (totalElapseMills < totalTimes) "<1" else totalElapseMills / totalTimes

    def reps(maxMethodSignLength: Int, maxClassLoaderLength: Int) =
      "%1$-" + maxMethodSignLength + "s    %2$-" + maxClassLoaderLength + "s    %3$#9s    %4$#9sms    %5$s" format(
        methodSign,
        klass.getClassLoader,
        totalTimes,
        avgElapseMillis,
        thisObject)

  }

}

class DetailWriter(writer: BufferedWriter) {
  def write(context: Context) {
    val started = "%1$tF %1$tT" format (new Date(context.started))
    val elapse = "%,dms" format (context.stopped.get - context.started)
    val thread = "[" + context.thread.getName + "]"
    val thisObject = if (context.thisObject == null) "null" else context.thisObject.toString
    val method = context.className + "." + context.methodName
    val arguments = context.arguments.mkString("[", " ", "]")
    val resultOrExcption = context.resultOrException match {
      case Some(x)                      => x.toString
      case None if context.isVoidReturn => "void"
      case None                         => "null"
    }
    val line = (started :: elapse :: thread :: thisObject :: method :: arguments :: resultOrExcption :: Nil).mkString(" ")
    writer.write(line)
    writer.newLine()
  }

  def close() {
    try {writer.close()} catch {case _ => }
  }
}

class StackWriter(writer: BufferedWriter) {
  def write(context: Context) {
    // TODO Avoid duplicated stack

    val arguments = context.arguments.map(o => simpleNameOf(o.getClass)).mkString("(", " ", ")")
    val head = "%1$s.%2$s%3$s call by thread [%4$s]"
      .format(context.className, context.methodName, arguments, context.thread.getName)

    writer.write(head)
    writer.newLine()
    context.stack foreach { s => writer.write("\t" + s); writer.newLine() }
    writer.newLine()
  }

  def close() {
    try {writer.close()} catch {case _ => }
  }
}
