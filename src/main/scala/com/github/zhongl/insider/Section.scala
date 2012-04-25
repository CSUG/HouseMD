package com.github.zhongl.insider

import java.io.PrintStream

object Section {
  def apply(head: String)(body: => Iterator[String])(implicit stream: PrintStream) {
    stream.println("##" + head)
    stream.println()
    body.foreach { s=>
      stream.println("\t" + s)
    }
    stream.println()
  }
}
