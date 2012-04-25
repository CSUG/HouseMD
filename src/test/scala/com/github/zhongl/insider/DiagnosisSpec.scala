package com.github.zhongl.insider

import java.lang.instrument._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class DiagnosisSpec extends FunSpec with ShouldMatchers {
  describe("Diagnosis") {
    it("output diagnosis report") {
      val inst = mock[Instrumentation]

      Diagnosis.probeWith(Array("-o","target/test-output/diagnosis.report","123","class"), inst)
    }
  }

}
