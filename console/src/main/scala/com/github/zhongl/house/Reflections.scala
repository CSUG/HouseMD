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

import System.identityHashCode
import java.lang._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

object Reflections {

  private val S = classOf[String]

  private val I = classOf[Integer]
  private val i = Integer.TYPE

  private val L = classOf[Long]
  private val l = Long.TYPE

  private val B = classOf[Boolean]
  private val b = Boolean.TYPE

  private val D = classOf[Double]
  private val d = Double.TYPE

  def convert(c: Class[_], s: String): AnyRef = {
    c match {
      case B | `b` => Boolean.valueOf(s)
      case I | `i` => Integer.valueOf(s)
      case L | `l` => Long.valueOf(s)
      case D | `d` => Double.valueOf(s)
      case S       => s
      case _       => throw new IllegalArgumentException("Unsupported converting type: " + c)
    }
  }

  def nativeToStringOf(instance: AnyRef) = instance.getClass.getName + "@" + Integer
    .toHexString(identityHashCode(instance))

}
