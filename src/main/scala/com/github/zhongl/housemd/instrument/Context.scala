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

package com.github.zhongl.housemd.instrument

import java.util


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
case class Context(
  className: String,
  methodName: String,
  loader: ClassLoader,
  arguments: Array[String],
  descriptor: String,
  isVoidReturn: Boolean,
  thisObject: AnyRef,
  started: Long,
  stack: Array[StackTraceElement],
  thread: Thread,
  stopped: Option[Long],
  resultOrException: Option[AnyRef])

object Context {

  implicit val map2Context:(java.util.Map[String,AnyRef] => Context) = apply(_: java.util.Map[String, AnyRef])

  def apply(map: java.util.Map[String, AnyRef]) = new Context(
    map.get(Advice.CLASS).asInstanceOf[String],
    map.get(Advice.METHOD).asInstanceOf[String],
    map.get(Advice.CLASS_LOADER).asInstanceOf[ClassLoader],
    map.get(Advice.ARGUMENTS).asInstanceOf[Array[AnyRef]].map(_.toString),
    map.get(Advice.DESCRIPTOR).asInstanceOf[String],
    map.get(Advice.VOID_RETURN).asInstanceOf[Boolean],
    map.get(Advice.THIS),
    map.get(Advice.STARTED).asInstanceOf[Long],
    map.get(Advice.STACK).asInstanceOf[Array[StackTraceElement]],
    map.get(Advice.THREAD).asInstanceOf[Thread],
    map.get(Advice.STOPPED) match {
      case v if v != null => Some(v.asInstanceOf[Long])
      case _              => None
    },
    map.get(Advice.RESULT) match {
      case v if v != null => Some(v)
      case _              => None
    }
  )
}