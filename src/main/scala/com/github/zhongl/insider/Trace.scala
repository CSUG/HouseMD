package com.github.zhongl.insider

import java.util.concurrent.TimeUnit._
import java.lang.System.{currentTimeMillis => now}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.LinkedBlockingQueue

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Trace[T](transformer: Transformer, timeout: Int, maxCount: Int) extends AbstractIterator[T] with Advice {

  // init code
  AdviceProxy.delegate = this
  transformer.probe()

  protected def computeNext(): Option[T] = {
    if (isTimeout || isOverCount) {
      transformer.reset()
      None
    } else {
      val next = queue.poll(500L, MILLISECONDS)
      if (next == null) computeNext() else Some(next)
    }
  }

  private[this] def isOverCount = count.incrementAndGet() > maxCount

  private[this] def isTimeout = now - started >= SECONDS.toMillis(timeout)

  def enterWith(context: Context) {}

  def exitWith(context: Context) {}

  private[this] lazy val queue   = new LinkedBlockingQueue[T]
  private[this] lazy val started = now
  private[this] lazy val count   = new AtomicInteger()

}

