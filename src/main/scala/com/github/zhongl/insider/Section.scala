package com.github.zhongl.insider

import java.io.PrintStream

object Section {
  def apply(head: String)(body: => Iterable[String]) = new Section(head, body)
}

class Section(head: String, body: => Iterable[String]) {
  def render(implicit stream: PrintStream) {
    stream.println("##" + head)
    stream.println
    body.foreach { s=>
      stream.println("\t" + s)
    }
    stream.println
  }
}