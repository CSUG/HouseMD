/*
 * Copyright 2012 zhongl
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
