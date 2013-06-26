package housemd

import sun.misc.Unsafe
import com.google.common.io.ByteStreams

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object Utils {
  def defineClass(klass: String, bytecode: Array[Byte]) = {
    val f = classOf[Unsafe].getDeclaredField("theUnsafe")
    f.setAccessible(true)
    val unsafe = f.get(null).asInstanceOf[Unsafe]
    unsafe.defineClass(klass, bytecode, 0, bytecode.size)
  }

  def bytecodeOf(name: String) = ByteStreams.toByteArray(getClass.getResourceAsStream(name))

}
