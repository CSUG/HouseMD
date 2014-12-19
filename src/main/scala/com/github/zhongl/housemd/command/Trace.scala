/*
 * Copyright 2013 zhongl
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
import com.cedarsoftware.util.io.JsonWriter
import com.github.zhongl.housemd.misc.ObjUtils
import com.github.zhongl.yascli.PrintOut
import com.github.zhongl.housemd.misc.ReflectionUtils._
import java.util.Date
import collection.immutable.SortedSet
import com.github.zhongl.housemd.instrument.{Hook, Context}
import org.objectweb.asm.Type
import java.io.{BufferedWriter, FileWriter, File}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Trace(val inst: Instrumentation, out: PrintOut)
  extends TransformCommand("trace", "display or output infomation of method invocaton.", inst, out)
          with MethodFilterCompleter {

  val outputRoot = {
    val dir = new File("/tmp/" + name + "/" + ManagementFactory.getRuntimeMXBean.getName)
    dir.mkdirs()
    dir
  }
  val detailFile = new File(outputRoot, "detail")
  val stackFile  = new File(outputRoot, "stack")

  private val detailable = flag("-d" :: "--detail" :: Nil, "enable append invocation detail to " + detailFile + ".")
  private val detailInJson = flag("-j" :: "--json" :: Nil, "convert detail info into json format.")
  private val stackable  = flag("-s" :: "--stack" :: Nil, "enable append invocation calling stack to " + stackFile + ".")

  private val methodFilters = parameter[Array[MethodFilter]]("method-filter", "method filter pattern like \"ClassSimpleName.methodName\" or \"ClassSimpleName\".")

  protected def isCandidate(klass: Class[_]) = methodFilters().find(_.filter(klass)).isDefined

  protected def isDecorating(klass: Class[_], methodName: String) =
    methodFilters().find(_.filter(klass, methodName)).isDefined

  override protected def hook = new Hook() {

    val enableDetail = detailable()
    val enableStack  = stackable()
    val showDetailInJson  = detailInJson()

    implicit val statisticOrdering = Ordering.by((_: Statistic).methodSign)

    var statistics           = SortedSet.empty[Statistic]
    var maxMethodSignLength  = 0
    var maxClassLoaderLength = 0

    if (showDetailInJson) ObjUtils.useJsonFormat() else ObjUtils.useToStringFormat()

    lazy val detailWriter = new DetailWriter(new BufferedWriter(new FileWriter(detailFile, true)))
    lazy val stackWriter  = new StackWriter(new BufferedWriter(new FileWriter(stackFile, true)))

    override def enterWith(context: Context) {}

    override def exitWith(context: Context) {
      if (enableDetail) detailWriter.write(context)
      if (enableStack) stackWriter.write(context)

      val statistic = new Statistic(context, 1L, context.stopped.get - context.started)

      maxClassLoaderLength = math.max(maxClassLoaderLength, statistic.loader.length)
      maxMethodSignLength = math.max(maxMethodSignLength, statistic.methodSign.length)

      statistics = statistics find { _.filter(context) } match {
        case Some(s) => (statistics - s) + (s + statistic)
        case None    => statistics + statistic
      }
    }

    override def heartbeat(now: Long) {
      if (statistics.isEmpty) println("No traced method invoked")
      else statistics foreach { s => println(s.reps(maxMethodSignLength, maxClassLoaderLength)) }
      println()
    }

    override def finalize(throwable: Option[Throwable]) {
      heartbeat(0L) // last print
      if (enableDetail) {
        detailWriter.close()
        info("You can get invocation detail from " + detailFile)
      }
      if (enableStack) {
        stackWriter.close()
        info("You can get invocation stack from " + stackFile)
      }
    }
  }

  class Statistic(val context: Context, val totalTimes: Long, val totalElapseMills: Long) {

    lazy val methodSign = "%1$s.%2$s(%3$s)".format(
      simpleNameOf(context.className),
      context.methodName,
      Type.getArgumentTypes(context.descriptor).map(t => simpleNameOf(t.getClassName)).mkString(", ")
    )

    lazy val loader = if (context.loader == null) "BootClassLoader" else getOrForceToNativeString(context.loader)

    private val NaN = "-"

    private lazy val thisObjectString = context match {
      case c if c.thisObject == null => "[Static Method]"
      case c if isInit(c.methodName) => "[Initialize Method]"
      case _                         => getOrForceToNativeString(context.thisObject)
    }

    private lazy val avgElapseMillis =
      if (totalTimes == 0) NaN else if (totalElapseMills < totalTimes) "<1" else totalElapseMills / totalTimes

    def +(s: Statistic) = new Statistic(context, totalTimes + s.totalTimes, totalElapseMills + s.totalElapseMills)

    def filter(context: Context) = this.context.loader == context.loader &&
                                   this.context.className == context.className &&
                                   this.context.methodName == context.methodName &&
                                   this.context.arguments.size == context.arguments.size &&
                                   this.context.descriptor == context.descriptor &&
                                   (isInit(context.methodName) || this.context.thisObject == context.thisObject)

    def reps(maxMethodSignLength: Int, maxClassLoaderLength: Int) =
      "%1$-" + maxMethodSignLength + "s    %2$-" + maxClassLoaderLength + "s    %3$9s    %4$9sms    %5$s" format(
        methodSign,
        loader,
        totalTimes,
        avgElapseMillis,
        thisObjectString)

    private def isInit(method: String) = method == "<init>"

  }

}

class DetailWriter(writer: BufferedWriter) {
  def write(context: Context) {
    val started = "%1$tF %1$tT" format (new Date(context.started))
    val elapse = "%,dms" format (context.stopped.get - context.started)
    val thread = "[" + context.thread.getName + "]"
    val thisObject = if (context.thisObject == null) "null" else context.thisObject.toString
    val method = context.className + "." + context.methodName
    val arguments = context.arguments.mkString("[" + ObjUtils.parameterSeparator, ObjUtils.parameterSeparator, "]")
    val resultOrExcption = context.resultOrException match {
      case Some(x)                      => ObjUtils.toString(x)
      case None if context.isVoidReturn => "void"
      case None                         => "null"
    }

    val argumentsAndResult = "Arguments: " + arguments + ObjUtils.parameterSeparator + "Result: " + resultOrExcption

    val line = (started :: elapse :: thread :: thisObject :: method :: argumentsAndResult :: Nil)
      .mkString(" ")
    writer.write(line)
    writer.newLine()

    context.resultOrException match {
      case Some(x) if x.isInstanceOf[Throwable] => x.asInstanceOf[Throwable].getStackTrace.foreach {
        s =>
          writer.write("\tat " + s)
          writer.newLine()
      }
      case _                                    =>
    }
  }

  def close() {
    try {writer.close()} catch {case _ => }
  }
}

class StackWriter(writer: BufferedWriter) {
  def write(context: Context) {
    // TODO Avoid duplicated stack

    val head = "%1$s.%2$s%3$s call by thread [%4$s]"
      .format(context.className, context.methodName, context.descriptor, context.thread.getName)

    writer.write(head)
    writer.newLine()
    context.stack foreach { s => writer.write("\t" + s); writer.newLine() }
    writer.newLine()
  }

  def close() {
    try {writer.close()} catch {case _ => }
  }
}
