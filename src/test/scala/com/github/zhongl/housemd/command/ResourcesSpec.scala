package com.github.zhongl.housemd.command

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.ByteArrayOutputStream
import org.mockito.Mockito._
import instrument.Instrumentation
import com.github.zhongl.test.A
import com.github.zhongl.yascli.PrintOut

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class ResourcesSpec extends FunSpec with ShouldMatchers{
  describe("Resources") {
    it("list source path of res.xml") {
      val out = new ByteArrayOutputStream
      val inst = mock(classOf[Instrumentation])
      doReturn(Array(classOf[A])).when(inst).getAllLoadedClasses

      val resources = new Resources(inst, PrintOut(out))

      resources parse("/com/github/zhongl/test/A.class".split("\\s+"))

      resources run()

      out.toString should be ("res.xml")
    }
  }

}
