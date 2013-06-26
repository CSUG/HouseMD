package housemd

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.google.common.io.ByteStreams
import sun.misc.Unsafe

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class ProbeSpec extends FunSpec with ShouldMatchers {

  describe("An probe") {
    it("should get invocation context") {
      val method = "nothing"

      val bytecode = Probe("housemd.ProbeTarget", _ == method, 0)(bytecodeOf("/housemd/ProbeTarget.class"))

      val c = defineClass("housemd.ProbeTarget", bytecode)

      val o = c.newInstance()

      c.getMethod(method).invoke(o)

      val event = Global.QUEUE.poll().asInstanceOf[Array[AnyRef]]

      event should be(Array(
        "housemd/ProbeTarget",
        method,
        "()V",
        o,
        o.getClass.getClassLoader,
        Thread.currentThread(),
        false,
        null,
        null,
        -1L,
        null)
      )
    }
  }


  def defineClass(klass: String, bytecode: Array[Byte]): Class[_] = {
    val f = classOf[Unsafe].getDeclaredField("theUnsafe")
    f.setAccessible(true)
    val unsafe = f.get(null).asInstanceOf[Unsafe]
    unsafe.defineClass(klass, bytecode, 0, bytecode.size)
  }

  def bytecodeOf(name: String) = ByteStreams.toByteArray(getClass.getResourceAsStream(name))
}
