package housemd

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import housemd.Utils._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class ProbeSpec extends FunSpec with ShouldMatchers {

  Global.AGENT_THREAD = new Thread()

  val probedClass = {
    val klass = "housemd.ProbeTarget"
    val filter: MethodFilter = _ != "<init>"
    val options = Global.OP_RESULT | Global.OP_ARGS
    val bytecode = Probe(klass, filter, options)(bytecodeOf("/housemd/ProbeTarget.class"))
    Probed(defineClass(klass, bytecode))
  }

  describe("A probe") {
    it("should get invocation context of ProbeTarget.nothing()") {
      val name = "nothing"
      probedClass.method(name).invoke()
      Global.QUEUE.poll() should be(event(method = name, descriptor = "()V", exception = false, result = null))
    }

    it("should get invocation context of ProbeTarget.error()") {
      val name = "error"
      var error: Throwable = null

      try {
        probedClass.method(name).invoke()
      } catch {
        case t: Throwable => error = t.getCause
      }

      Global.QUEUE.poll() should be(event(method = name, descriptor = "()V", exception = true, result = error))

    }

    it("should get invocation context of ProbeTarget.value()") {
      val name = "value"
      probedClass.method(name).invoke()
      Global.QUEUE.poll() should be(event(method = name, descriptor = "()I", exception = false, result = 1))
    }
  }


  def event(method: String, descriptor: String, exception: Boolean, result: Any) = Array(
    "housemd/ProbeTarget",
    method,
    descriptor,
    probedClass.instance,
    probedClass.loader,
    Thread.currentThread(),
    exception,
    Array.empty,
    result,
    -1L,
    null
  )
}
