package com.github.zhongl.insider

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.TimeUnit

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object HaltAdviceProxy {
  def apply(delegate: HaltAdvice, timeout: Int, maxCount: Int)(haltCallback: Cause => Unit) =
    new HaltAdviceProxy(delegate, timeout, maxCount, haltCallback)
}

class HaltAdviceProxy(delegate: HaltAdvice, timeout: Int, maxCount: Int, haltCallback: Cause => Unit)
  extends Advice {

  def haltAndCallback(cause: Cause) {
    delegate.halt()
    haltCallback(cause)
  }

  def enterWith(context: Context) {
    if (isTimeout) haltAndCallback(Timeout(timeout))
    else if (overMaxCount) haltAndCallback(Over(maxCount))
    else `catch` {delegate.enterWith(context)}
  }

  def exitWith(context: Context) { `catch` {delegate.exitWith(context)}}

  private[this] def isTimeout: Boolean = (System.nanoTime() - started) >= TimeUnit.SECONDS.toNanos(timeout)

  private[this] def overMaxCount: Boolean = count.incrementAndGet() > maxCount

  private[this] def `catch`(snippet: => Unit) {
    try { snippet} catch { case t => haltAndCallback(Thrown(t)) }
  }

  private[this] lazy val count = new AtomicInteger()
  private[this] lazy val started = System.nanoTime()
}

trait HaltAdvice extends Advice {
  def halt()
}

trait Cause

case class Timeout(seconds: Int) extends Cause

case class Over(maxCount: Int) extends Cause

case class Thrown(t: Throwable) extends Cause
