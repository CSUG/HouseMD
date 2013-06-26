package housemd

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.google.common.io.ByteStreams
import sun.misc.Unsafe

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class ProbeSpec extends FunSpec with ShouldMatchers {

  Global.AGENT_THREAD = new Thread()

  describe("A probe") {
    it("should get invocation context of ProbeTarget.nothing()") {

      val method = "nothing"

      probedTargetClass.getMethod(method).invoke(probedTargetInstance)

      val event = Global.QUEUE.poll().asInstanceOf[Array[AnyRef]]

      event should be(Array(
        "housemd/ProbeTarget",
        method,
        "()V",
        probedTargetInstance,
        probedTargetClass.getClassLoader,
        Thread.currentThread(),
        false,
        null,
        null,
        -1L,
        null)
      )
    }

    it("should get invocation context of ProbeTarget.error()") {
      val method = "error"
      var error: Throwable = null

      try {
        probedTargetClass.getMethod(method).invoke(probedTargetInstance)
      } catch {
        case t: Throwable => error = t.getCause
      }

      val event = Global.QUEUE.poll().asInstanceOf[Array[AnyRef]]

      event should be(Array(
        "housemd/ProbeTarget",
        method,
        "()V",
        probedTargetInstance,
        probedTargetClass.getClassLoader,
        Thread.currentThread(),
        true,
        null,
        error,
        -1L,
        null)
      )

    }

    it("should get invocation context of ProbeTarget.value()") {
      val method = "value"

      try {
        probedTargetClass.getMethod(method).invoke(probedTargetInstance)
      } catch {
        case _: Throwable =>
      }

      val event = Global.QUEUE.poll().asInstanceOf[Array[AnyRef]]

      event should be(Array(
        "housemd/ProbeTarget",
        method,
        "()I",
        probedTargetInstance,
        probedTargetClass.getClassLoader,
        Thread.currentThread(),
        false,
        null,
        1,
        -1L,
        null)
      )

    }
  }


  lazy val probedTargetClass = {
    val klass = "housemd.ProbeTarget"
    val bytecode = Probe(klass,
      m => m != "<init>",
      Global.OP_RESULT)(bytecodeOf("/housemd/ProbeTarget.class"))
    defineClass(klass, bytecode)
  }

  lazy val probedTargetInstance = probedTargetClass.newInstance()

  def defineClass(klass: String, bytecode: Array[Byte]): Class[_] = {
    val f = classOf[Unsafe].getDeclaredField("theUnsafe")
    f.setAccessible(true)
    val unsafe = f.get(null).asInstanceOf[Unsafe]
    unsafe.defineClass(klass, bytecode, 0, bytecode.size)
  }

  def bytecodeOf(name: String) = ByteStreams.toByteArray(getClass.getResourceAsStream(name))
}
