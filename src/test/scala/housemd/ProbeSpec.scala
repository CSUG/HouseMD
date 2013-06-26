package housemd

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class ProbeSpec extends FunSpec with ShouldMatchers {

  Global.AGENT_THREAD = new Thread()

  describe("A probe") {
    it("should get invocation context of ProbeTarget.nothing()") {
      val name = "nothing"
      Probed.method(name).invoke()
      Global.QUEUE.poll() should be(event(method = name, descriptor = "()V", exception = false, result = null))
    }

    it("should get invocation context of ProbeTarget.error()") {
      val name = "error"
      var error: Throwable = null

      try {
        Probed.method(name).invoke()
      } catch {
        case t: Throwable => error = t.getCause
      }

      Global.QUEUE.poll() should be(event(method = name, descriptor = "()V", exception = true, result = error))

    }

    it("should get invocation context of ProbeTarget.value()") {
      val name = "value"
      Probed.method(name).invoke()
      Global.QUEUE.poll() should be(event(method = name, descriptor = "()I", exception = false, result = 1))
    }
  }


  def event(method: String, descriptor: String, exception: Boolean, result: Any) = Array(
    "housemd/ProbeTarget",
    method,
    descriptor,
    Probed.instance,
    Probed.loader,
    Thread.currentThread(),
    exception,
    Array.empty,
    result,
    -1L,
    null
  )
}
