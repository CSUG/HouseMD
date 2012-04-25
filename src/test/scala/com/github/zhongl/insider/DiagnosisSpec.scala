package com.github.zhongl.insider

import java.lang.instrument._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar._
import io.Source

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class DiagnosisSpec extends FunSpec with ShouldMatchers {
  describe("Diagnosis") {
    it("output diagnosis report") {
      val inst = mock[Instrumentation]
      val path = "target/test-output/diagnosis.report"

      Diagnosis.probeWith(Array("-o",path,"123","class"), inst)

      val report = Source.fromFile(path).getLines().toTraversable

      report should (
        contain ("#Enviroment") and
        contain ("#Properties")
      )
    }
  }

}
