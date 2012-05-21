package com.github.zhongl

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

object Convertors {
//  implicit val string2String  = (s: String) => s
  implicit val string2Int     = (s: String) => s.toInt
  implicit val string2Boolean = (s: String) => s.toBoolean
}
