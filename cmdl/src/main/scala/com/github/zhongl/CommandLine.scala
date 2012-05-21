package com.github.zhongl

import collection.mutable.{ListBuffer, Map}
import annotation.tailrec

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
trait CommandLine {

  val name       : String
  val version    : String
  val description: String

  private val options    = ListBuffer.empty[Option]
  private val parameters = ListBuffer.empty[Parameter]
  private val values     = Map.empty[String, String]

  final def main(arguments: Array[String])(implicit output: String => Unit) {
    try {
      parse(arguments)
      run
    } catch {
      case UnknownOptionException(name)     => output("Unknown option: " + name)
      case MissingParameterException(name)  => output("Missing parameter: " + name)
      case ConvertingException(name, value) => output("Invalid " + name + " value: " + value)
    }
  }

  final def help() {
    // TODO
  }

  protected def run()

  protected final def flag(names: List[String], description: String) = {
    import Convertors.string2Boolean

    option[Boolean](names, description, Some(false))
  }

  protected final def option[T](names: List[String], description: String, defaultValue: scala.Option[T] = None)
    (implicit convert: String => T) = {

    checkIllegalOption(names)
    checkDuplicatedOption(names)
    options += Option(names, description, defaultValue)
    () => eval(names(0), defaultValue)
  }

  protected final def parameter[T](name: String, description: String, defaultValue: scala.Option[T] = None)
    (implicit convert: String => T) = {

    checkDuplicatedParameter(name)
    parameters += Parameter(name, description)
    () => eval(name)
  }

  private def checkIllegalOption(names: List[String]) {
    if (names.isEmpty) throw new IllegalArgumentException("At least one name should be given to option.")
    names.foreach { name =>
      if (!name.startsWith("-")) throw new IllegalArgumentException(name + " should starts with '-'.")
    }
  }

  private def checkDuplicatedParameter(s: String) {
    parameters.find(_.name == name) match {
      case None    =>
      case Some(_) => throw new IllegalStateException(s + " have already been used")
    }
  }

  private def checkDuplicatedOption[T](names: scala.List[String]) {
    options.find(_.names.intersect(names).size > 0) match {
      case None         =>
      case Some(option) => throw new IllegalStateException(names + " have already been used in " + option.names)
    }
  }

  private def parse(arguments: Array[String]) {

    @tailrec
    def read(list: List[String])(implicit index: Int = 0) {
      list match {
        case head :: tail if (head.matches("-[a-zA-Z-]+")) => read(addOption(head, tail))
        case Nil                                           => // end recusive
        case _                                             => read(addParameter(index, list))(index + 1)
      }
    }

    read(arguments.toList)
  }

  def addOption(name: String, rest: List[String]) = {
    options find (_.names.contains(name)) match {
      case None         => throw new UnknownOptionException(name)
      case Some(option) => option.defaultValue match {
        case Some(false) => values(option.names(0)) = "true"; rest // flag option
        case _           => values(option.names(0)) = rest.head; rest.tail
      }
    }
  }

  def addParameter(index: Int, arguments: List[String]) = {
    val parameter = parameters(index)
    values(parameter.name) = arguments.head
    arguments.tail
  }

  private def eval[T](name: String, defaultValue: scala.Option[T] = None)
    (implicit convert: String => T) = values get name match {
    case None        => defaultValue.getOrElse {throw MissingParameterException(name)}
    case Some(value) => try convert(value) catch {case _ => throw ConvertingException(name, value) }
  }

  case class Option(names: List[String], description: String, defaultValue: scala.Option[_])

  case class Parameter(name: String, description: String)

}

case class UnknownOptionException(name: String) extends Exception

case class MissingParameterException(name: String) extends Exception

case class ConvertingException(name: String, value: String) extends Exception