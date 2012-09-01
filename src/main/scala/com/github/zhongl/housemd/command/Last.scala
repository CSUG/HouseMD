package com.github.zhongl.housemd.command

import com.github.zhongl.yascli.{PrintOut, Command}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Last(out: PrintOut) extends Command("last","show exception stack trace of last error.", out) {

  private var stackTraces:Array[StackTraceElement] = _

  def keep(throwable: Throwable) {
    stackTraces = throwable.getStackTrace
  }


  def run() {
    if (stackTraces != null) stackTraces foreach { s => println("\t" + s) }
    else println("There is no error.")
  }
}
