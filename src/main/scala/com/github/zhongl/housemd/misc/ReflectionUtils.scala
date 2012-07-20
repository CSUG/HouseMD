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

package com.github.zhongl.housemd.misc

import System.identityHashCode
import java.lang._
import java.lang.reflect.{Method, Modifier}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

object ReflectionUtils {

  private val S = classOf[String]

  private val I = classOf[Integer]
  private val i = Integer.TYPE

  private val L = classOf[Long]
  private val l = Long.TYPE

  private val T = classOf[Boolean]
  private val t = Boolean.TYPE

  private val D = classOf[Double]
  private val d = Double.TYPE

  private val F = classOf[Float]
  private val f = Float.TYPE

  private val C = classOf[Char]
  private val c = Character.TYPE

  private val B = classOf[Byte]
  private val b = Byte.TYPE

  private lazy val defineClassMethod = {
    val int = Integer.TYPE
    val objects = classOf[Array[scala.Byte]]
    val string = classOf[String]
    val m = classOf[ClassLoader].getDeclaredMethod("defineClass", string, objects, int, int)
    m.setAccessible(true)
    m
  }

  def toBoxClass(k: Class[_]) = k match {
    case `i` => I
    case `l` => L
    case `d` => D
    case `f` => F
    case `t` => T
    case `b` => B
    case `c` => C
    case _   => k
  }

  def toNativeString(instance: AnyRef) =
    instance.getClass.getName + "@" + Integer.toHexString(identityHashCode(instance))

  def getOrForceToNativeString(instance: AnyRef) =
    if (instance.toString.startsWith(instance.getClass + "@")) instance.toString else toNativeString(instance)

  /**see https://github.com/zhongl/HouseMD/issues/17 */
  def loadOrDefine(clazz: Class[_], inClassLoader: ClassLoader) = {
    val name = clazz.getName
    try {
      inClassLoader.loadClass(name)
    } catch {
      case e: ClassNotFoundException =>
        import Utils._

        val bytes = toBytes(clazz.getResourceAsStream("/" + name.replace('.', '/') + ".class"))
        val zero: java.lang.Integer = 0
        val length: java.lang.Integer = bytes.length

        defineClassMethod.invoke(inClassLoader, name, bytes, zero, length).asInstanceOf[Class[_]]
    }
  }

  def simpleNameOf(c: Class[_]): String = simpleNameOf(c.getName)

  def simpleNameOf(className: String) = className.split("\\.").last

  def isNotConcrete(c: Class[_]) = c.isInterface || Modifier.isAbstract(c.getModifiers)

  def isAbstract(m: Method) = Modifier.isAbstract(m.getModifiers)

  def isFromBootClassLoader(c: Class[_]) = c.getClassLoader == null

  def constructorAndMethodNamesOf(c: Class[_]): Array[String] = {
    val names = c.getDeclaredMethods.map(_.getName)
    if (!c.isInterface) names :+ "<init>" else names
  }

}
