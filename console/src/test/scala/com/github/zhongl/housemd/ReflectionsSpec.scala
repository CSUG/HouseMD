package com.github.zhongl.housemd

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class ReflectionsSpec extends FunSpec with ShouldMatchers {
  describe("Reflection") {
    it("should load Advice") {
      Reflections.loadOrDefine(classOf[Advice], new ClassLoader(getClass.getClassLoader) {}) should be(classOf[Advice])
    }
    it("should define Advice") {
      Reflections.loadOrDefine(classOf[Advice], new ClassLoader() {
        override def loadClass(name: String) = {
          if (name == classOf[Advice].getName) throw new ClassNotFoundException()
          else super.loadClass(name)
        }
      })
    }
  }

}
