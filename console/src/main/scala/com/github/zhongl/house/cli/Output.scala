package com.github.zhongl.house.cli

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
trait Output {
  def print(text: String)

  def println(line: String)

  def printf(pattern: String, objs: AnyRef*)
}
