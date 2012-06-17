package com.github.zhongl.housemd.command

import java.lang.reflect.{Method, Type}
import com.github.zhongl.housemd.misc.Reflections._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class MethodFilter(classSimpleName: String, methodName: String = "*") {
  implicit val type2Class = (_: Type).asInstanceOf[Class[_]]

  def lazyFilter(className: String, superClassName: String, interfaceNames: Array[String]): (String => Boolean) =
    lazyFilter(filterOnly(className, superClassName, interfaceNames))

  def filter(c: Class[_], m: Method): Boolean = if (isAbstract(m)) false else lazyFilter(c)(m.getName)

  def filter(c: Class[_]): Boolean = {
    if (!filterOnly(c)) false
    else {
      val filter = lazyFilter(true)(_)
      c.getDeclaredMethods.find { m => filter(m.getName) }.isDefined
    }
  }

  private def lazyFilter(cond: => Boolean)(m: String) = if (cond) {methodName == "*" || methodName == m} else false

  private def lazyFilter(c: Class[_]): (String => Boolean) = lazyFilter(filterOnly(c))

  private def filterOnly(className: String, superClassName: String, interfaceNames: Array[String]): Boolean = {
    if (classSimpleName.endsWith("+")) {
      val realname = classSimpleName.dropRight(1)
      (simpleNameOf(className) == realname ||
        superClassName != null && simpleNameOf(superClassName) == realname) ||
        interfaceNames.find(simpleNameOf(_) == realname).isDefined
    } else {
      simpleNameOf(className) == classSimpleName
    }
  }

  private def filterOnly(c: Class[_]): Boolean = {
    val superClassName = if (c.getSuperclass == null) null else c.getSuperclass.getName
    val interfaceNames = c.getInterfaces.map(_.getName)
    filterOnly(c.getName, superClassName, interfaceNames)
  }

}

object MethodFilter {

  implicit def apply(s: String) = {
    s.split("\\.") match {
      case Array(classSimpleName, methodName) => new MethodFilter(classSimpleName, methodName)
      case Array(classSimpleName)             => new MethodFilter(classSimpleName)
      case _                                  =>
        throw new IllegalArgumentException(", it should be \"ClassSimpleName.methodName\" or \"ClassSimpleName\"")
    }
  }

  implicit val string2MethodFilters = (_: String).split("\\s+") map {apply}
}

trait MethodFilterCompleter extends ClassSimpleNameCompleter {

  import java.util.List

  object MethodName {
    def unapply(m: Method) = Some(m.getName)
  }

  override protected def completeClassSimpleName(buffer: String, cursor: Int, candidates: List[CharSequence]) =
    buffer.split("\\.") match {
      case Array(classSimpleName) if buffer.endsWith(".") => completeAll(classSimpleName, cursor, candidates)
      case Array(prefix)                                  => super.completeClassSimpleName(buffer, cursor, candidates)
      case Array(classSimpleName, methodPrefix)           => complete(classSimpleName, methodPrefix, cursor, candidates)
      case _                                              => -1
    }

  override protected def collectLoadedClassNames(prefix: String) = inst.getAllLoadedClasses collect {
    case c@ClassSimpleName(n) if n.startsWith(prefix) && isNotConcrete(c) => n + "+"
    case ClassSimpleName(n) if n.startsWith(prefix)                       => n
  }

  private def allDeclaredMethodsOf(classSimpleName: String)(collect: Array[Method] => Array[String]) =
    inst.getAllLoadedClasses collect {
      case c@ClassSimpleName(n) if (n == classSimpleName || n + "+" == classSimpleName) => collect(c.getDeclaredMethods)
    }

  private def completeAll(classSimpleName: String, cursor: Int, candidates: List[CharSequence]) =
    allDeclaredMethodsOf(classSimpleName) {_ map {_.getName}} match {
      case Array() => -1
      case all     => all.flatten.sorted foreach {candidates.add}; cursor
    }

  private def complete(classSimpleName: String, methodPrefix: String, cursor: Int, candidates: List[CharSequence]) =
    allDeclaredMethodsOf(classSimpleName) {_ collect {case MethodName(m) if m.startsWith(methodPrefix) => m }} match {
      case Array() => -1
      case all     => all.flatten.sorted foreach {candidates.add}; cursor - methodPrefix.length
    }
}