package com.github.zhongl.housemd

import org.scalatest.FunSpec
import com.github.zhongl.yascli.PrintOut
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import instrument.Instrumentation
import java.io.ByteArrayOutputStream
import annotation.tailrec

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class LoadedSpec extends FunSpec with ShouldMatchers {
  describe("Loaded") {
    it("should display the source jar of String") {
      parseAndRun("String@java.lang") {_ should startWith("java.lang.String -> ")}
    }

    it("should display the classloader hierarchies") {
      parseAndRun("-h Loaded@com.github.zhongl.housemd") { out =>
        val lines = out.split("\n")
        lines.head should startWith("com.github.zhongl.housemd.Loaded -> ")

        @tailrec
        def eq(list: List[String], classLoader: ClassLoader) {
          list match {
            case head :: tail => head should endWith(classLoader.toString); eq(tail, classLoader.getParent)
            case Nil          => // end
          }
        }

        eq(lines.tail.toList, classOf[Loaded].getClassLoader)
      }
    }

  }


  def parseAndRun(arguments: String)(verify: String => Unit) {
    val inst = mock(classOf[Instrumentation])
    val out = new ByteArrayOutputStream()

    doReturn(Array(classOf[String], classOf[Loaded])).when(inst).getAllLoadedClasses

    val loaded = new Loaded(inst, PrintOut(out))

    loaded.parse(arguments.split("\\s+"))
    loaded.run()
    verify(out.toString)
  }
}
