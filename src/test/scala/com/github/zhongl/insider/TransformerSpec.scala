package com.github.zhongl.insider

import org.scalatest.FunSpec
import org.mockito.Mockito._
import instrument.Instrumentation

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class TransformerSpec extends FunSpec {

  describe("Transformer") {
    it("should probe classes that filtered by method regex patterns"){
      val inst = mock(classOf[Instrumentation])
      val methodRegexs = Array(".+split.*")

      doReturn(Array(classOf[String], classOf[java.lang.Integer])).when(inst).getAllLoadedClasses

      val transformer = new Transformer(inst, methodRegexs)
      transformer.probe()

      verify(inst).retransformClasses(classOf[String])
    }
  }

}
