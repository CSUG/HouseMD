package com.github.zhongl.housemd.command

import instrument.Instrumentation
import com.github.zhongl.yascli.{Command, PrintOut}
import collection.JavaConversions._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Resources (inst: Instrumentation, out: PrintOut)
  extends Command("resources", "list source paths by resource name.", out) {

  private val resourceName = parameter[String]("name", "resource name.")

  private object Loader {
    def unapply(c: Class[_]) = if (c.getClassLoader == null) None else Some(c.getClassLoader)
  }

  def run() {
    val name = resourceName()
    val loaders = inst.getAllLoadedClasses collect {case Loader(cl) => cl }
    loaders foreach println
    println(name)
    val enumerations = loaders.distinct map {_.getResources(name)}
    val urls = for (e <- enumerations; url <- e) yield url

    urls.distinct foreach println
  }
}
