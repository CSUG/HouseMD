package com.github.zhongl.house.cli

import com.github.zhongl.house.Advice

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
trait VirtualMachine {
  def allLoadedClasses: Array[Class[_]]

  def probe(advice: Advice)

  def reset()
}
