package com.github.zhongl.house

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import instrument.Instrumentation
import collection.mutable.ListBuffer

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class LoadedClassesSpec extends FunSpec with ShouldMatchers {
  describe("LoadedClasses") {
    it("should output loaded classes info") {
      val lines = ListBuffer.empty[String]
      val inst = mock(classOf[Instrumentation])
      val stringClass = classOf[String]

      doReturn(Array(stringClass)) when (inst) getAllLoadedClasses

      new LoadedClasses().apply(inst) {
        lines += _
      }

      lines should contain(stringClass.getName + " -> " + Utils.sourceOf(stringClass))
    }

    it("should output loaded classes info and it's class loader hierarchies") {
      val lines = ListBuffer.empty[String]
      val inst = mock(classOf[Instrumentation])
      val klass = classOf[LoadedClasses]

      doReturn(Array(klass)) when (inst) getAllLoadedClasses

      new LoadedClasses(loaderHierarchies = true).apply(inst) {
        lines += _
      }

      lines should {
        contain(klass.getName + " -> " + Utils.sourceOf(klass)) and
          contain("\t- " + klass.getClassLoader) and
          contain("\t\t- " + klass.getClassLoader.getParent) and
          contain("\t\t\t- " + klass.getClassLoader.getParent.getParent) and
          contain("\t\t\t\t- " + klass.getClassLoader.getParent.getParent.getParent) and
          contain("\t\t\t\t\t- " + klass.getClassLoader.getParent.getParent.getParent.getParent)
      }
    }
  }
}
