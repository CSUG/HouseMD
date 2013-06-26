package housemd

import org.objectweb.asm._
import org.objectweb.asm.Opcodes._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
abstract class Transform extends (Bytecode => Bytecode) {

  def apply(b: Bytecode): Bytecode = {
    val cw = new ClassWriter(ClassWriter.COMPUTE_MAXS)
    val cv = new ClassVisitor(ASM4, cw) {
      override def visitMethod(access: Int, name: String, desc: String, signature: String, exceptions: Array[String]) =
        decorate(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc)
    }

    new ClassReader(b).accept(cv, ClassReader.EXPAND_FRAMES)
    cw.toByteArray
  }

  protected def decorate(mv: MethodVisitor, access: Int, name: String, desc: String): MethodVisitor
}
