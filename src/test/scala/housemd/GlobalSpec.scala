package housemd

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class GlobalSpec extends FunSpec with ShouldMatchers {
  describe("Global") {
    it("should skip offer if agent thread is current") {
      Global.AGENT_THREAD = Thread.currentThread()
      Global.offer("klass", "method", "desc", null, null, 0, null, null, -1L)
      Global.QUEUE.poll() should be(null)
    }

    it("should skip offer if self is queue") {
      Global.AGENT_THREAD = new Thread()
      Global.offer("klass", "method", "desc", Global.QUEUE, null, 0, null, null, -1L)
      Global.QUEUE.poll() should be(null)
    }

    it("should skip offer if it recusive while probe Throwable") { pending }

    it("should offer null agent thread event") {
      Global.AGENT_THREAD = null
      Global.offer("klass", "method", "desc", null, null, 0, null, null, -1L)
      Global.QUEUE.poll() should be(Global.NULL_AGENT_THREAD)
    }
  }
}
