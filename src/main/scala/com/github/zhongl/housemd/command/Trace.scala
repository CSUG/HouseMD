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

package com.github.zhongl.housemd.command

import instrument.Instrumentation
import management.ManagementFactory
import com.github.zhongl.yascli.PrintOut
import java.io.{BufferedWriter, FileWriter, File}
import com.github.zhongl.housemd.misc.Reflections._
import java.util.Date
import collection.immutable.SortedSet
import java.util.regex.Pattern
import com.github.zhongl.housemd.instrument.{Filter, Seconds, Hook, Context}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Trace(val inst: Instrumentation, out: PrintOut)
  extends TransformCommand("trace", "display or output infomation of method invocaton.", inst, out)
          with MethodFilterCompleter {

  import com.github.zhongl.yascli.Converters._

  val outputRoot = {
    val dir = new File("/tmp/" + name + "/" + ManagementFactory.getRuntimeMXBean.getName)
    dir.mkdirs()
    dir
  }
  val detailFile = new File(outputRoot, "detail")
  val stackFile  = new File(outputRoot, "stack")

  private val _detailable = flag("-d" :: "--detail" :: Nil, "enable append invocation detail to " + detailFile + ".")
  private val _stackable  = flag("-s" :: "--stack" :: Nil, "enable append invocation calling stack to " + stackFile + ".")

  private val _packagePattern = option[Pattern]("-p" :: "--package" :: Nil, "package regex pattern for filtering.", ".*")
  private val _interval       = option[Seconds]("-i" :: "--interval" :: Nil, "display trace statistics interval.", 1)
  private val _timeout        = option[Seconds]("-t" :: "--timeout" :: Nil, "limited trace seconds.", 10)
  private val _overLimit      = option[Int]("-l" :: "--limit" :: Nil, "limited limited times.", 1000)

  private val methodFilters = parameter[Array[MethodFilter]]("method-filter", "method filter pattern like \"ClassSimpleName.methodName\" or \"ClassSimpleName\".")

  override protected def filter = new Filter() {
    val mfs = methodFilters()

    def apply(klass: Class[_]) = {

      @inline
      def matchesPackagePattern = {
        val p = _packagePattern()
        p.pattern() == ".*" || (klass.getPackage != null && p.matcher(klass.getPackage.getName).matches())
      }

      @inline
      def includeByMethodFilters = mfs.find(_.filter(klass)).isDefined

      matchesPackagePattern && includeByMethodFilters
    }

    def apply(klass: Class[_], methodName: String) = mfs.find(_.filter(klass, methodName)).isDefined
  }

  override protected def timeout = _timeout()

  override protected def overLimit = _overLimit()

  override protected def hook = new Hook() {
    var last = System.currentTimeMillis()

    val enableDetail   = _detailable()
    val enableStack    = _stackable()
    val intervalMillis = _interval().toMillis

    implicit val statisticOrdering = Ordering.by((_: Statistic).methodSign)

    var statistics           = SortedSet.empty[Statistic]
    var maxMethodSignLength  = 0
    var maxClassLoaderLength = 0

    lazy val detailWriter = new DetailWriter(new BufferedWriter(new FileWriter(detailFile, true)))
    lazy val stackWriter  = new StackWriter(new BufferedWriter(new FileWriter(stackFile, true)))

    override def enterWith(context: Context) {}

    override def exitWith(context: Context) {
      if (enableDetail) detailWriter.write(context)
      if (enableStack) stackWriter.write(context)

      val statistic = new Statistic(context, 1L, context.stopped.get - context.started)

      maxClassLoaderLength = math.max(maxClassLoaderLength, statistic.loader.length)
      maxMethodSignLength = math.max(maxMethodSignLength, statistic.methodSign.length)

      statistics = statistics find {!_.filter(context)} match {
        case Some(s) => statistics - s + (s + statistic)
        case None    => statistics + statistic
      }
    }

    override def heartbeat(now: Long) {
      if (now - last >= intervalMillis) {
        last = now
        if (statistics.isEmpty) println("No traced method invoked")
        else statistics foreach { s => println(s.reps(maxMethodSignLength, maxClassLoaderLength)) }
        println()
      }
    }

    override def end(throwable: Option[Throwable]) {
      if (enableDetail) detailWriter.close()
      if (enableStack) stackWriter.close()
    }
  }

  class Statistic(val context: Context, val totalTimes: Long, val totalElapseMills: Long) {

    lazy val methodSign = "%1$s.%2$s(%3$s)".format(
      simpleNameOf(context.className),
      context.methodName,
      context.arguments.map(o => simpleNameOf(o.getClass)).mkString(", ")
    )

    lazy val loader = if (context.loader == null) "BootClassLoader" else context.loader.toString

    private val NaN = "-"

    private lazy val thisObjectString = if (context.thisObject == null) "[Static Method]" else context.thisObject.toString

    private lazy val avgElapseMillis =
      if (totalTimes == 0) NaN else if (totalElapseMills < totalTimes) "<1" else totalElapseMills / totalTimes

    def +(s: Statistic) = new Statistic(context, totalTimes + s.totalTimes, totalElapseMills + s.totalElapseMills)

    def filter(context: Context) = this.context.loader == context.loader &&
                                   this.context.className == context.className &&
                                   this.context.methodName == context.methodName &&
                                   this.context.arguments.size == context.arguments.size &&
                                   this.context.arguments.map(_.getClass) == context.arguments.map(_.getClass) &&
                                   this.context.thisObject == context.thisObject

    def reps(maxMethodSignLength: Int, maxClassLoaderLength: Int) =
      "%1$-" + maxMethodSignLength + "s    %2$-" + maxClassLoaderLength + "s    %3$#9s    %4$#9sms    %5$s" format(
        methodSign,
        loader,
        totalTimes,
        avgElapseMillis,
        thisObjectString)

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
