package housemd

import org.objectweb.asm.{Label, Type, MethodVisitor}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.commons.{Method, AdviceAdapter}
import org.objectweb.asm.Type._
import scala.reflect.ClassTag


trait Chase extends Decorator {

  def filter: MethodFilter

  /**
   * Returns an option code  in:
   *
   * - [[housemd.Global.OP_ARGS]]
   * - [[housemd.Global.OP_ELAPSE]]
   * - [[housemd.Global.OP_EXCEPTION]]
   * - [[housemd.Global.OP_STACK]]
   * - [[housemd.Global.OP_RESULT]]
   *
   * @return option code
   */
  def options: Int

  def apply(mv: MethodVisitor, klass: String, access: Int, method: String, desc: String): MethodVisitor = {

    if (!filter(method)) return mv

    @inline def isNot(code: Int) = (access & code) == 0
    @inline def is(code: Int) = !isNot(code)

    require(mv != null, "Probing method visitor should not be null.")
    require(isNot(ACC_ABSTRACT), "Probing method should not be abstract.")
    require(isNot(ACC_NATIVE), "Probing method should not be native.")

    new AdviceAdapter(ASM4, mv, access, method, desc) {
      val start          = new Label
      val end            = new Label
      val getClassLoader = new Method("getClassLoader", "()Ljava/lang/ClassLoader;")
      val offer          = new Method("offer", "(Ljava/lang/String;" +
                                               "Ljava/lang/String;" +
                                               "Ljava/lang/String;" +
                                               "Ljava/lang/Object;" +
                                               "Ljava/lang/ClassLoader;" +
                                               "I" +
                                               "[Ljava/lang/Object;" +
                                               "Ljava/lang/Object;" +
                                               "J)V")

      override def onMethodEnter() {
        mark(start)
      }

      override def onMethodExit(opcode: Int) {
        if (opcode == ATHROW) return

        invokeGlobalOffer(options) { dupResult(opcode, desc) }
      }

      override def visitMaxs(maxStack: Int, maxLocals: Int) {
        mark(end)
        catchException(start, end, `type`[Throwable])

        invokeGlobalOffer(options | Global.OP_EXCEPTION) { dup() }

        throwException()
        super.visitMaxs(maxStack, maxLocals)
      }

      def invokeGlobalOffer(options: Int)(dupResultOrException: => Unit) {
        push(klass)
        push(method)
        push(desc)
        loadThisOrPushNullIfIsStatic()
        loadClassLoader()
        push(options)
        loadArgArray()
        dupResultOrException
        push(-1L) // elapse

        invokeStatic(`type`[Global], offer)
      }

      def loadClassLoader() {
        push(`type`(klass))
        invokeVirtual(`type`[Class[_]], getClassLoader)
      }

      def `type`(name: String) = Type.getObjectType(name)

      def `type`[T](implicit t: ClassTag[T]) = Type.getType(t.runtimeClass)

      def dupResult(opcode: Int, desc: String) {
        opcode match {
          case RETURN            => pushNull() // void
          case ARETURN           => dup() // object
          case LRETURN | DRETURN => dup2(); box(getReturnType(desc)) // long or double
          case _                 => dup(); box(getReturnType(desc)) // object or boolean or byte or char or short or int
        }
      }

      def pushNull() { push(null.asInstanceOf[Type]) }

      def loadThisOrPushNullIfIsStatic() {
        if (is(ACC_STATIC)) pushNull() else loadThis()
      }
    }
  }
}
