package com.github.zhongl.house

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

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
