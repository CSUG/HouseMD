package com.github.zhongl.insider

abstract class AbstractIterator[+T] extends Iterator[T] {
  private[this] var ref: Option[T] = Option.empty[T]

  protected def computeNext(): Option[T]

  def hasNext = synchronized {
    ref match {
      case Some(_) => true
      case None    => ref = computeNext(); !ref.isEmpty
    }
  }

  def next(): T = synchronized {
    ref match {
      case Some(x) => ref = None; x
      case None    => ref = computeNext(); if (ref.isEmpty) null.asInstanceOf[T] else next()
    }
  }

}
