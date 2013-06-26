import org.objectweb.asm.MethodVisitor

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
package object housemd {
  type Bytecode = Array[Byte]

  type MethodFilter = String => Boolean

  type Decorator = (MethodVisitor, String, Int, String, String) => MethodVisitor

  type Transformer = Bytecode => Bytecode
}
