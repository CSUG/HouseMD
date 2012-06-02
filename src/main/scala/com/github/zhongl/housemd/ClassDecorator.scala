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

import org.objectweb.asm._
import commons.{Method, AdviceAdapter}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.Type._
import java.util.regex.Pattern
import scala.Predef._

object ClassDecorator {

  def decorate(classfileBuffer: Array[Byte], toDecorateMethodRegexs: Array[Pattern]) = {
    val cr: ClassReader = new ClassReader(classfileBuffer)
    val cw: ClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
    cr.accept(classAdapter(cw, toDecorateMethodRegexs), ClassReader.EXPAND_FRAMES)
    cw.toByteArray
  }

  def classAdapter(cw: ClassWriter, methodRegexs: Array[Pattern]) =
    new ClassAdapter(cw) {

      override def visit(
        version: Int,
        access: Int,
        name: String,
        signature: String,
        superName: String,
        interfaces: Array[String]) {
        className = name.replace("/", ".")
        super.visit(version, access, name, signature, superName, interfaces)
      }

      override def visitMethod(
        access: Int,
        name: String,
        desc: String,
        signature: String,
        exceptions: Array[String]) = {
        val mv = super.visitMethod(access, name, desc, signature, exceptions)
        if ((mv != null && containsMethod(name))) methodAdapter(mv, access, name, desc) else mv
      }

      private[this] def containsMethod(methodName: String) = methodRegexs.find(_.matcher(methodName).matches).isDefined

      private[this] def methodAdapter(mv: MethodVisitor, access: Int, methodName: String, desc: String): MethodAdapter =
        new AdviceAdapter(mv, access, methodName, desc) {
          val advice         = Type.getType(classOf[Advice])
          val enter          = Method.getMethod(Advice.ON_METHOD_BEGIN)
          val exit           = Method.getMethod(Advice.ON_METHOD_END)

          override def visitMaxs(maxStack: Int, maxLocals: Int) {
            mark(end)
            catchException(start, end, Type.getType(classOf[Throwable]))
            dup()
            invokeStatic(advice, exit)
            throwException()
            super.visitMaxs(maxStack, maxLocals)
          }

          protected override def onMethodEnter() {
            push(className)
            push(methodName)
            push(methodDesc)
            loadThisOrPushNullIfIsStatic()
            loadArgArray()
            invokeStatic(advice, enter)
            mark(start)
          }

          protected override def onMethodExit(opcode: Int) {
            if (opcode != ATHROW) {
              prepareResultBy(opcode)
              invokeStatic(advice, exit)
            }
          }

          private[this] def isStaticMethod = (methodAccess & ACC_STATIC) != 0

          private[this] def loadThisOrPushNullIfIsStatic() { if (isStaticMethod) pushNull() else loadThis() }

          private[this] def prepareResultBy(opcode: Int) {
            opcode match {
              case RETURN            => pushNull() // void
              case ARETURN           => dup() // object
              case LRETURN | DRETURN => dup2(); box(getReturnType(methodDesc)) // long or double
              case _                 => dup(); box(getReturnType(methodDesc)) // object or boolean or byte or char or short or int
            }
          }

          private[this] def pushNull() { push(null.asInstanceOf[Type]) }

          private[this] val start = new Label
          private[this] val end   = new Label
        }

      private[this] var className: String = _

    }
}
