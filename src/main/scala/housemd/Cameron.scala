package housemd

import org.objectweb.asm._
import org.objectweb.asm.Opcodes._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
trait Cameron extends Transformer {
  self: Decorator =>

  def apply(b: Bytecode) = {
    val cw = new ClassWriter(ClassWriter.COMPUTE_MAXS)
    val cv = new ClassVisitor(ASM4, cw) {

      var klass: String = _

      override def visit(
        version: Int,
        access: Int,
        name: String,
        signature: String,
        superName: String,
        interfaces: Array[String]) {
        klass = name
        super.visit(version, access, name, signature, superName, interfaces)
      }

      override def visitMethod(access: Int, name: String, desc: String, signature: String, exceptions: Array[String]) =
        apply(super.visitMethod(access, name, desc, signature, exceptions), klass, access, name, desc)
    }

    new ClassReader(b).accept(cv, ClassReader.EXPAND_FRAMES)
    cw.toByteArray
  }
}
