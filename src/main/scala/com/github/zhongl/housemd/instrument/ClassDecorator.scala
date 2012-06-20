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

import org.objectweb.asm._
import commons.{Method, AdviceAdapter}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.Type._

object ClassDecorator {

  def decorate(classfileBuffer: Array[Byte], className: String, methodFilter: (String => Boolean)) = {
    val cr: ClassReader = new ClassReader(classfileBuffer)
    val cw: ClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
    cr.accept(classAdapter(cw, className, methodFilter), ClassReader.EXPAND_FRAMES)
    cw.toByteArray
  }

  def classAdapter(cw: ClassWriter, className: String, filter: (String => Boolean)) =
    new ClassAdapter(cw) {

      override def visitMethod(acc: Int, name: String, desc: String, sign: String, exces: Array[String]) = {
        val mv = super.visitMethod(acc, name, desc, sign, exces)
        if ((mv != null && isNotAbstract(acc) && filter(name))) methodAdapter(mv, acc, name, desc) else mv
      }

      private def isNotAbstract(acc: Int) = (Opcodes.ACC_ABSTRACT & acc) == 0

      private[this] def methodAdapter(mv: MethodVisitor, access: Int, methodName: String, desc: String) =
        new AdviceAdapter(mv, access, methodName, desc) {
          val advice = Type.getType(classOf[Advice])
          val enter  = Method.getMethod(Advice.ON_METHOD_BEGIN)
          val exit   = Method.getMethod(Advice.ON_METHOD_END)

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

    }
}
