package com.github.zhongl.insider

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.TimeUnit

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object HaltAdviceProxy {
  def apply(delegate: Advice, timeout: Int, maxCount: Int)(haltCallback: PartialFunction[Cause, Unit]) =
    new HaltAdviceProxy(delegate, timeout, maxCount, haltCallback)
}

class HaltAdviceProxy(delegate: Advice, timeout: Int, maxCount: Int, haltCallback: PartialFunction[Cause, Unit])
  extends Advice {

  def enterWith(context: Context) {
    if (isTimeout) haltCallback(Timeout(timeout))
    else if (overMaxCount) haltCallback(Over(maxCount))
    else `catch` {delegate.enterWith(context)}
  }

  def exitWith(context: Context) {
    `catch` {delegate.exitWith(context)}
  }

  private[this] def isTimeout: Boolean = (System.nanoTime() - started) >= TimeUnit.SECONDS.toNanos(timeout)

  private[this] def overMaxCount: Boolean = count.incrementAndGet() > maxCount

  private[this] def `catch`(snippet: => Unit) {
    try { snippet} catch { case t => haltCallback(Thrown(t)) }
  }

  private[this] lazy val count = new AtomicInteger()
  private[this] lazy val started = System.nanoTime()
}

trait Cause

case class Timeout(seconds: Int) extends Cause

case class Over(maxCount: Int) extends Cause

case class Thrown(t: Throwable) extends Cause
