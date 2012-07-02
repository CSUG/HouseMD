package com.github.zhongl.housemd.command

import instrument.Instrumentation
import com.github.zhongl.yascli.{Command, PrintOut}
import collection.JavaConversions._
import org.reflections.Reflections
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder}
import org.reflections.scanners.ResourcesScanner
import com.google.common.base.Predicate

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Resources (inst: Instrumentation, out: PrintOut)
  extends Command("resources", "list source paths by resource name.", out) {

  private val regex = parameter[String]("regex", "resource name regex pattern.")

  private object Loader {
    def unapply(c: Class[_]) = if (c.getClassLoader == null) None else Some(c.getClassLoader)
  }

  def run() {
    val r = regex()
    val loaders = inst.getAllLoadedClasses collect {case Loader(cl) => cl }
    val config = new ConfigurationBuilder().setUrls(ClasspathHelper.forClassLoader(loaders: _*)).setScanners(new ResourcesScanner())
    val reflections = new Reflections(config)

    val files = reflections.getResources(java.util.regex.Pattern.compile(r))

    files foreach println
  }
}
