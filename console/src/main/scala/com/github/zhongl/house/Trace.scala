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
import java.lang.System.{currentTimeMillis => now}
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.LinkedBlockingQueue

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object Trace extends AbstractIterator[String] with HaltAdvice {

  def enterWith(context: Context) {}

  def exitWith(context: Context) {
    val started = "%1$tF %1$tT" format (new Date(context.startAt))
    val elapse = "%,dms" format (context.stopAt - context.startAt)
    val thread = Thread.currentThread().getName
    val method = context.className + "." + context.methodName
    val arguments = context.arguments.mkString(" ")
    val resultOrExcption = context.resultOrException match {
      case null => "null"
      case x => x.toString
    }
    queue.offer(Array(started, elapse, thread, method, arguments, resultOrExcption).mkString(" "))
  }

  def halt() {isHalt.set(true)}

  protected def computeNext(): Option[String] = {
    if (isHalt.get()) {
      None
    } else {
      val next = queue.poll(500L, MILLISECONDS)
      if (next == null) computeNext() else Some(next)
    }
  }

  private[this] lazy val queue = new LinkedBlockingQueue[String]

  private[this] lazy val isHalt = new AtomicBoolean(false)
}

