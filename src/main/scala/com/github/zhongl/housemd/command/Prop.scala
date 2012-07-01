package com.github.zhongl.housemd.command

import com.github.zhongl.yascli.{Command, PrintOut}
import jline.console.completer.Completer
import collection.JavaConversions._


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Prop(out: PrintOut) extends Command("prop", "display system properties.", out) with Completer {

  private val regexable = flag("-e" :: "--regex" :: Nil, "enable name as regex pattern.")

  private val keyName = parameter[String]("name", "system env key name.")

  def run() {
    if (regexable()) listEnvMatchs(keyName()) else printPropEquals(keyName())
  }

  private def printPropEquals(key: String) {
    val value = System.getProperty(key)
    if (value == null) println("Invalid key " + key)
    else println(key + " = " + value)
  }

  private def listEnvMatchs(regex: String) {
    val matched = System.getProperties.stringPropertyNames.toList filter {_.matches(regex)}
    val pattern = "%1$-" + matched.maxBy(_.length).length + "s = %2$s"
    matched.sorted.foreach { k => println(pattern.format(k, System.getProperty(k))) }
  }

  def complete(buffer: String, cursor: Int, candidates: java.util.List[CharSequence]) = {
    val names = System.getProperties.stringPropertyNames.toList filter (_.startsWith(buffer))
    names.sorted foreach candidates.add
    if (candidates.isEmpty) -1 else cursor - buffer.length
  }
}
