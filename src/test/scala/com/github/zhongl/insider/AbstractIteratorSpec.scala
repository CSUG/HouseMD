package com.github.zhongl.insider

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import java.util.concurrent.LinkedBlockingQueue
import sun.net.dns.ResolverConfiguration.Options
import java.util.concurrent.atomic.AtomicReference

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class AbstractIteratorSpec extends FunSpec with ShouldMatchers {

  describe("AbstractIterator") {

    it("should hasNext more time without next") {
      val iterator = new AbstractIterator[String] {
        def computeNext = Some("")
      }
      iterator.hasNext should be (true)
      iterator.hasNext should be (true)
    }

    it("should not hasNext after next") {

      var value = Option("")

      val iterator = new AbstractIterator[String] {
        protected def computeNext = value
      }

      iterator.next should be ("")

      value = None
      iterator.next should be (null)
      iterator.hasNext should be (false)
    }
  }

}
