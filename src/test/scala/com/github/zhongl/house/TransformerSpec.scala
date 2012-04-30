package com.github.zhongl.house

import org.scalatest.FunSpec
import org.mockito.Mockito._
import org.mockito.ArgumentCaptor
import instrument.{ClassDefinition, Instrumentation}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class TransformerSpec extends FunSpec {

  class Mock1
  class Mock2

  describe("Transformer") {
    it("should probe classes that filtered by method regex patterns"){
      val inst = mock(classOf[Instrumentation])
      val methodRegexs = Array(".*Mock1.*")

      doReturn(Array(classOf[Mock1],classOf[Mock2])).when(inst).getAllLoadedClasses

      val transformer = new Transformer(inst, methodRegexs, ".")
      transformer.probe()

      val argument = ArgumentCaptor.forClass(classOf[ClassDefinition])
      verify(inst).redefineClasses(argument.capture())

      assert(argument.getValue.getDefinitionClass === classOf[Mock1])
    }
  }

}
