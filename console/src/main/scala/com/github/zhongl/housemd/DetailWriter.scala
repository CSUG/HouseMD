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

import java.io.BufferedWriter
import java.util.Date

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class DetailWriter(writer: BufferedWriter) {
  def write(context: Context) {
    val started = "%1$tF %1$tT" format (new Date(context.started))
    val elapse = "%,dms" format (context.stopped.get - context.started)
    val thread = context.thread.getName
    val method = context.className + "." + context.methodName
    val arguments = context.arguments.mkString("[", " ", "]")
    val resultOrExcption = context.resultOrException match {
      case None    => "null"
      case Some(x) => x.toString
    }
    val line = (started :: elapse :: thread :: method :: arguments :: resultOrExcption :: Nil).mkString(" ")
    writer.write(line)
    writer.newLine()
  }

  def close() {
    try {writer.close()} catch {case _ => }
  }
}
