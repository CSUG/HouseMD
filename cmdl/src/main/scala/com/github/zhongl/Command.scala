/*
 * Copyright 2012 zhongl
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.zhongl

import collection.mutable.{ListBuffer, Map}
import annotation.tailrec
import java.text.MessageFormat

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
abstract class Command(val name: String, val description: String) {

  private val options    = ListBuffer.empty[Option[_]]
  private val parameters = ListBuffer.empty[Parameter[_]]
  private val values     = Map.empty[String, String]

  def help = MessageFormat.format(
    """Usage  : {0} [OPTIONS] {1}
      |{2}
      |Options:
      |{3}
      |Parameters:
      |{4}
      | """.stripMargin,
    name,
    parameters.map(_.name).mkString(" "),
    "\t" + description,
    options.mkString("\n"),
    parameters.mkString("\n"))

  def parse(arguments: Array[String]) {

    @tailrec
    def read(list: List[String])(implicit index: Int = 0) {
      list match {
        case head :: tail if (head.matches("-[a-zA-Z-]+")) => read(addOption(head, tail))
        case head :: tail                                  => read(addParameter(index, list))(index + 1)
        case Nil                                           => // end recusive
      }
    }

    read(arguments.toList)
  }

  def run()

  protected final def flag(names: List[String], description: String) = {
    import Convertors.string2Boolean

    option[Boolean](names, description, false)
  }

  protected final def option[T](names: List[String], description: String, defaultValue: T)
    (implicit m: Manifest[T], convert: String => T) = {

    checkIllegalOption(names)
    checkDuplicatedOption(names)
    options += Option[T](names, description, defaultValue)
    () => eval(names(0), Some(defaultValue))
  }

  protected final def parameter[T](name: String, description: String)(implicit m: Manifest[T], convert: String => T) = {
    checkDuplicatedParameter(name)
    parameters += Parameter[T](name, description)
    () => eval(name)
  }

  private def checkIllegalOption(names: List[String]) {
    if (names.isEmpty) throw new IllegalArgumentException("At least one name should be given to option.")
    names find (!_.startsWith("-")) match {
      case Some(n) => throw new IllegalArgumentException(n + " should starts with '-'.")
      case None    => // ignore
    }
  }

  private def checkDuplicatedParameter(s: String) {
    if (parameters.find(_.name == name).isDefined) throw new IllegalStateException(s + " have already been used")
  }

  private def checkDuplicatedOption[T](names: scala.List[String]) {
    options.find(_.names.intersect(names).size > 0) match {
      case Some(option) => throw new IllegalStateException(names + " have already been used in " + option.names)
      case None         => // ignore
    }
  }

  private def addOption(name: String, rest: List[String]) = options find (_.names.contains(name)) match {
    case Some(option) if option.isFlag => values(option.names(0)) = "true"; rest
    case Some(option)                  => values(option.names(0)) = rest.head; rest.tail
    case None                          => throw new UnknownOptionException(name)
  }

  private def addParameter(index: Int, arguments: List[String]) = parameters(index) match {
    case p if p.isVarLength => values(p.name) = arguments.mkString(" "); Nil
    case p                  => values(p.name) = arguments.head; arguments.tail
  }

  private def eval[T](name: String, defaultValue: scala.Option[T] = None)
    (implicit convert: String => T) = values get name match {
    case None        => defaultValue.getOrElse {throw MissingParameterException(name)}
    case Some(value) => try {convert(value)} catch {
      case t: Throwable => throw ConvertingException(name, value, t.getMessage)
    }
  }

  case class Option[T: Manifest](names: List[String], description: String, defaultValue: T) {
    override def toString = {
      def ?(s: => String) = if (isFlag) "" else s

      "\t" + names.mkString(", ") + ?("=[" + manifest[T].erasure.getSimpleName.toUpperCase + "]") +
        "\n\t\t" + description +
        ?("\n\t\tdefault: " + defaultValue)
    }

    def isFlag = manifest[T].erasure == classOf[Boolean]
  }

  case class Parameter[T: Manifest](name: String, description: String) {
    override def toString = "\t" + name + "\n\t\t" + description

    def isVarLength = manifest[T].erasure.isArray
  }

}

case class UnknownOptionException(name: String) extends Exception

case class MissingParameterException(name: String) extends Exception

case class ConvertingException(name: String, value: String, explain: String) extends Exception