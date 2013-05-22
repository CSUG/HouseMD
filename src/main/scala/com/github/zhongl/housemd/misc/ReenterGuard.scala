package com.github.zhongl.housemd.misc

import scala.annotation.tailrec

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object ReenterGuard {

  private[this] var slots = Array.fill(firstPrimeGreatThan(Thread.activeCount()))(Option.empty[Int])

  private[this] val lock = new Object

  def enter() = lock.synchronized {
    withCurrentThreadHashCodeAndSlotIndex {
      (hashcode, index) => slots(index) match {
        case None                     => slots(index) = Some(hashcode); true
        case Some(c) if c == hashcode => false
        case _                        => enlarge(); slots(index) = Some(hashcode); true
      }
    }
  }

  def leave() {
    lock.synchronized {
      withCurrentThreadHashCodeAndSlotIndex { (_, index) => slots(index) = None }
    }
  }

  private def withCurrentThreadHashCodeAndSlotIndex[T](f: (Int, Int) => T) = {
    val hashcode = System.identityHashCode(Thread.currentThread())
    f(hashcode, hashcode % slots.length)
  }

  private def enlarge() {
    val s = Array.fill(firstPrimeGreatThan(slots.length * 2))(Option.empty[Int])
    slots foreach { _ foreach (h => s(h % s.length) = Some(h)) }
    slots = s
  }

  private def firstPrimeGreatThan(num: Int): Int = {

    @tailrec
    def rec(s: Stream[Int], f: Int => Boolean): Int = {
      val h = s.head
      if (f(h)) h else rec(s.tail filter (_ % h != 0), f)
    }

    rec(Stream from 2, _ > num)
  }
}
