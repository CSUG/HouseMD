package com.github.zhongl

import collection.mutable.{ListBuffer, Map}

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
      case e: MissingException    => output("Missing value of " + e.name)
      case e: ConvertingException => output("Invalid " + e.name + " value: " + e.value)
    }
  }

  final def help(){
    // TODO
  }

  protected def run()

  protected final def option[T](name: String, description: String, defaultValue: scala.Option[T] = None)
    (implicit convert: String => T): () => T = {
    val shortName = short(name)
    options += Option(name, shortName, description, defaultValue)
    val reps = shortName match {
      case None    => "--" + name
      case Some(c) => "-" + c + " or --" + name
    }
    () => eval(reps, defaultValue)
  }

  protected final def parameter[T](name: String, description: String)
    (implicit convert: String => T): () => T = {
    parameters += Parameter(name, description)
    () => eval(name)
  }

  private def parse(arguments: Array[String]) {
    // TODO
  }

  private def short(name: String): scala.Option[Char] = {
    // TODO
    None
  }

  private def eval[T](name: String, defaultValue: scala.Option[T] = None)
    (implicit convert: String => T) = values get name match {
    case None        => defaultValue.getOrElse {throw new MissingException(name)}
    case Some(value) => try convert(value) catch {case t: Throwable => throw new ConvertingException(name, value, t) }
  }

  case class Option(name: String, short: scala.Option[Char], description: String, defaultValue: scala.Option[_])

  case class Parameter(name: String, description: String)

}


class MissingException(val name: String) extends Exception

class ConvertingException(val name: String, val value: String, t: Throwable) extends Exception(t)