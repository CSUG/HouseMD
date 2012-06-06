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

  // FIXME this is ugly, because i don't know how to get class of byte[]
  private lazy val defineClassMethod = {
    val m = classOf[ClassLoader].getDeclaredMethods.find { m =>
      m.getName == "defineClass" && (m.getParameterTypes match {
        case Array(S, _, `i`, `i`) => true
        case _                     => false
      })
    }.get
    m.setAccessible(true)
    m
  }

  def nativeToStringOf(instance: AnyRef) = instance.getClass.getName + "@" + Integer
    .toHexString(identityHashCode(instance))

  /**see https://github.com/zhongl/HouseMD/issues/17 */
  def loadOrDefine(clazz: Class[_], inClassLoader: ClassLoader) = {
    val name = clazz.getName
    try {
      inClassLoader.loadClass(name)
    } catch {
      case _: ClassNotFoundException =>
        import Utils._

        val bytes = toBytes(clazz.getResourceAsStream("/" + name.replace('.', '/') + ".class"))
        val zero: java.lang.Integer = 0
        val length: java.lang.Integer = bytes.length

        defineClassMethod.invoke(inClassLoader, name, bytes, zero, length).asInstanceOf[Class[_]]
    }
  }

  def simpleNameOf(c: Class[_]):String = simpleNameOf(c.getName)

  def simpleNameOf(className: String) = className.split("\\.").last

  def allMethodsOf(c: Class[_]) = (c.getMethods ++ c.getDeclaredMethods).toSet
}
