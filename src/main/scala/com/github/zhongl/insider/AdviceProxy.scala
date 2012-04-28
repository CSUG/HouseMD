package com.github.zhongl.insider

import org.objectweb.asm._
import collection.mutable.Stack
import java.lang.System.{nanoTime => now}
import commons.Method
import java.lang.Object
import java.util.concurrent.atomic.AtomicReference

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object AdviceProxy {

  def onMethodBegin(
                     className: String,
                     methodName: String,
                     descriptor: String,
                     thisObject: Object,
                     arguments: Array[Object]) {
    val voidReturn = Type.getReturnType(descriptor).equals(Type.VOID_TYPE);
    val context = new Context(className, methodName, voidReturn, thisObject, arguments, currentStrackTrace());
    try {
      delegate.enterWith(context);
    } catch {
      case _ =>
      // TODO notify to stop probing and reset
    }
    context.startAt = now;
    stackPush(context);
  }

  def onMethodEnd(resultOrException: Object) {
    val context = stackPop()
    context.stopAt = now
    context.resultOrException = resultOrException;

    try {
      delegate.exitWith(context);
    } catch {
      case _ =>
      // TODO notify to stop probing and reset
    }
  }

  def clearThreadBoundContext() {threadBoundContexts.clear()}

  def delegate_=(instance: Advice) {_delegate.set(instance)}

  def delegate = _delegate.get

  private[this] def currentStrackTrace() = {
    val stackTrace = Thread.currentThread().getStackTrace
    java.util.Arrays.copyOfRange(stackTrace, 4, stackTrace.length) // trim useless stack trace elements.
  }

  private[this] def method(name: String, argumentTypes: Class[_]*) =
    new Method(name, Type.getMethodDescriptor(this.getClass.getMethod(name, argumentTypes: _*)))

  private[this] def stackPush(c: Context) =
    threadBoundContexts.getOrElseUpdate(Thread.currentThread(), new Stack[Context]).push(c)

  private[this] def stackPop() = threadBoundContexts(Thread.currentThread()).pop()

  lazy val ENTRY = method("onMethodBegin", classOf[String], classOf[String], classOf[String], classOf[Object], classOf[Object])
  lazy val EXIT  = method("onMethodEnd", classOf[Object])
  lazy val TYPE  = Type.getType(this.getClass)

  private[this] val threadBoundContexts = collection.mutable.Map.empty[Thread, Stack[Context]]
  private[this] val _delegate           = new AtomicReference[Advice](NullAdvice)
}

case class Context(
                    className: String,
                    methodName: String,
                    voidReturn: Boolean,
                    thisObject: Object,
                    arguments: Array[Object],
                    stackTrace: Array[StackTraceElement]) {
  var startAt          : Long   = 0L
  var stopAt           : Long   = 0L
  var resultOrException: Object = _
}

object NullAdvice extends Advice {
  def enterWith(context: Context) {}

  def exitWith(context: Context) {}
}

trait Advice {
  def enterWith(context: Context)

  def exitWith(context: Context)
}
