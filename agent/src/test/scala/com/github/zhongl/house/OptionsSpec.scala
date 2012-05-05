package com.github.zhongl.house

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.File

class OptionsSpec extends FunSpec with ShouldMatchers {

  val options = Options.parse("class.loader.urls=a.jar:b.jar closure.executor.name=com.github.zhongl.house.Executor console.address=localhost:54321")

  def url(s: String) = new File(s).toURI.toURL

  describe("Options") {
    it("should get class loader urls") {
      options.classLoaderUrls() should {
        contain(url("a.jar")) and contain(url("b.jar")) and have size (2)
      }
    }

    it("should get closure executor name") {
      options.closureExecutorName() should be ("com.github.zhongl.house.Executor")
    }

    it("should get console address") {
      options.consoleAddress() should be ("localhost:54321")
    }
  }
}
