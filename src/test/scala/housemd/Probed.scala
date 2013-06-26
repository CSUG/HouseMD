package housemd

import sun.misc.Unsafe
import java.lang.reflect.Method
import com.google.common.io.ByteStreams

object Probed {
  val clazz = {
    val klass = "housemd.ProbeTarget"
    val filter: MethodFilter = _ != "<init>"
    val options = Global.OP_RESULT | Global.OP_ARGS
    val bytecode = Probe(klass, filter, options)(bytecodeOf("/housemd/ProbeTarget.class"))
    defineClass(klass, bytecode)
  }

  val instance = clazz.newInstance.asInstanceOf[AnyRef]

  val loader = clazz.getClassLoader

  def method(name: String, classes: Class[_]*) = new Invocation(instance, clazz.getMethod(name, classes: _*))

  private def defineClass(klass: String, bytecode: Array[Byte]) = {
    val f = classOf[Unsafe].getDeclaredField("theUnsafe")
    f.setAccessible(true)
    val unsafe = f.get(null).asInstanceOf[Unsafe]
    unsafe.defineClass(klass, bytecode, 0, bytecode.size)
  }

  private def bytecodeOf(name: String) = ByteStreams.toByteArray(getClass.getResourceAsStream(name))

}

class Invocation(o: Object, method: Method) {
  def invoke(parameters: AnyRef*) = method.invoke(o, parameters: _*)
}
