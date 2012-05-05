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
import actors._
import actors.Actor._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object HaltAdviceProxy {
  def apply(delegate: HaltAdvice, timeout: Int, maxCount: Int)(haltCallback: Cause => Unit) =
    new HaltAdviceProxy(delegate, timeout, maxCount, haltCallback)
}

class HaltAdviceProxy(delegate: HaltAdvice, timeoutSeconds: Int, maxCount: Int, haltCallback: Cause => Unit)
  extends Advice {

  actor{
    receiveWithin(SECONDS.toMillis(timeoutSeconds)){
      case TIMEOUT => _timeout.set(true); haltAndCallback(Timeout(timeoutSeconds))
    }
  }

  def haltAndCallback(cause: Cause) {
    delegate.halt()
    haltCallback(cause)
  }

  def enterWith(context: Context) {
    if (isTimeout) Unit
    else if (overMaxCount) haltAndCallback(Over(maxCount))
    else `catch` {delegate.enterWith(context)}
  }

  def exitWith(context: Context) {`catch` {delegate.exitWith(context)}}

  private[this] def isTimeout: Boolean = _timeout.get()

  private[this] def overMaxCount: Boolean = count.incrementAndGet() > maxCount

  private[this] def `catch`(snippet: => Unit) {
    try {snippet} catch {case t => haltAndCallback(Thrown(t))}
  }

  private[this] lazy val count   = new AtomicInteger()
  private[this] lazy val _timeout = new AtomicBoolean(false)
}

trait HaltAdvice extends Advice {
  def halt()
}

trait Cause

case class Timeout(seconds: Int) extends Cause

case class Over(maxCount: Int) extends Cause

case class Thrown(t: Throwable) extends Cause
