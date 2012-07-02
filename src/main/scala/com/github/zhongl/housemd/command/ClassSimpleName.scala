package com.github.zhongl.housemd.command

import com.github.zhongl.housemd.misc.ReflectionUtils._
import jline.console.completer.Completer
import instrument.Instrumentation

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object ClassSimpleName {
  def unapply(c: Class[_]) = Some(simpleNameOf(c))
}

trait ClassSimpleNameCompleter extends Completer {
  val inst: Instrumentation

  def complete(buffer: String, cursor: Int, candidates: java.util.List[CharSequence]) = buffer.split("\\s+") match {
    case Array() => -1
    case all     => completeClassSimpleName(all.last, cursor, candidates)
  }

  protected def completeClassSimpleName(prefix: String, cursor: Int, candidates: java.util.List[CharSequence]): Int =
    collectLoadedClassNames(prefix) match {
      case Array() => -1
      case all     => all.distinct.sorted foreach {candidates.add}; cursor - prefix.length
    }

  protected def collectLoadedClassNames(prefix: String) = inst.getAllLoadedClasses collect {
    case ClassSimpleName(n) if n.startsWith(prefix) => n
  }
}

