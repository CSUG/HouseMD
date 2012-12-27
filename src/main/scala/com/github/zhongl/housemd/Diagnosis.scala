package com.github.zhongl.housemd

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object Diagnosis {

  trait Term

  case class Instruction(name: String, arguments: Array[String]) extends Term

  case class Feedback(content: String) extends Term

}
