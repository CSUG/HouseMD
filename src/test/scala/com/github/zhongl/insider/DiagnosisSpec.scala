package com.github.zhongl.insider

import java.lang.instrument._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import io.Source
import org.mockito.Mockito._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class DiagnosisSpec extends FunSpec with ShouldMatchers {
  describe("Diagnosis") {
    it("output diagnosis report") {
      val inst = mock(classOf[Instrumentation])
      val path = "target/test-output/diagnosis.report"

      doReturn(Array(classOf[Args], classOf[String])) when (inst) getAllLoadedClasses

      val args = Array("-l", "com.github.*", "-o", path, "123", "class")
      Diagnosis.probeWith(args, inst)

      val report = Source.fromFile(path).getLines().toTraversable

      report should (
          contain ("#Diagnosis report") and
          contain ("##Summary") and
          contain ("##Enviroment") and
          contain ("##Properties") and
          contain ("##Loaded classes") and
          contain ("\tcom.github.zhongl.insider.Args -> file:/home/jushi/dev/workspaces/scala-new/insider-pack/insider/target/scala-2.9.2/classes/com/github/zhongl/insider/Args.class")
        )
    }
  }

}
