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

      val event = Global.QUEUE.poll().asInstanceOf[Array[AnyRef]]

      event should be(Array(
        "housemd/ProbeTarget",
        name,
        "()V",
        Probed.instance,
        Probed.loader,
        Thread.currentThread(),
        false,
        Array.empty,
        null,
        -1L,
        null)
      )
    }

    it("should get invocation context of ProbeTarget.error()") {
      val name = "error"
      var error: Throwable = null

      try {
        Probed.method(name).invoke()
      } catch {
        case t: Throwable => error = t.getCause
      }

      val event = Global.QUEUE.poll().asInstanceOf[Array[AnyRef]]

      event should be(Array(
        "housemd/ProbeTarget",
        name,
        "()V",
        Probed.instance,
        Probed.loader,
        Thread.currentThread(),
        true,
        Array.empty,
        error,
        -1L,
        null)
      )

    }

    it("should get invocation context of ProbeTarget.value()") {
      val name = "value"

      Probed.method(name).invoke()

      val event = Global.QUEUE.poll().asInstanceOf[Array[AnyRef]]

      event should be(Array(
        "housemd/ProbeTarget",
        name,
        "()I",
        Probed.instance,
        Probed.loader,
        Thread.currentThread(),
        false,
        Array.empty,
        1,
        -1L,
        null)
      )

    }
  }

}
