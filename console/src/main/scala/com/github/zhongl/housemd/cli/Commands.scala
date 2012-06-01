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

package com.github.zhongl.housemd.cli

import java.lang.reflect.Method
import collection.JavaConversions._
import com.github.zhongl.housemd.logging.Loggable
import com.github.zhongl.housemd.Reflections._
import jline.console.completer.Completer
import java.util.{SortedSet, List}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
abstract class Commands(commandObjects: AnyRef*) extends Loggable with Completer {

  protected val name2Command = {
    val map = scala.collection.mutable.Map.empty[String, Command]
    commandObjects.toList ::: Quit :: Help :: Nil foreach { instance =>
      commandMethodOf(instance) match {
        case None         => warn("Skip invalid command {}", instance)
        case Some(method) =>
          val name = instance.getClass.getAnnotation(classOf[annotation.Command]).name()
          map += (name -> new Command(name, method, instance))
      }
    }
    map.toMap
  }

  protected val commandNames = new java.util.TreeSet[String](name2Command.keySet)

  def execute(name: String, arguments: String*) {
    name2Command get name match {
      case None          => warn("Unknown command {}", name)
      case Some(command) => command.apply(arguments)
    }
  }

  def commands: SortedSet[String] = commandNames

  def command(name: String): Option[Command] = name2Command get name

  private[this] def commandMethodOf(instance: AnyRef) = instance.getClass.getMethods find {_.getName == "apply"}

  class Command(name: String, method: Method, instance: AnyRef) extends (Seq[String] => Unit) with Completer {
    private lazy val completer = {
      if (instance.isInstanceOf[Completer]) instance.asInstanceOf[Completer]
      else new Completer {
        def complete(buffer: String, cursor: Int, candidates: List[CharSequence]) = -1
      }
    }

    def apply(argStrings: Seq[String]) {
      try invoke(parse(argStrings)) catch {
        case e: IllegalArgumentException => warn(e.getMessage); Help.apply(name) //; throw e
        case e: Throwable                => warn(e.getMessage) //; throw e
      }
    }

    def complete(buffer: String, cursor: Int, candidates: List[CharSequence]) =
      completer.complete(buffer, cursor, candidates)


    private[this] def invoke(arguments: Seq[AnyRef]) {
      method.invoke(instance, arguments: _*)
    }

    private[this] def parse(argStrings: Seq[String]): Seq[AnyRef] = {
      val classes = method.getParameterTypes
      if (argStrings.size != classes.size) throw new IllegalArgumentException("Miss match argument")
      classes zip argStrings map { t => convert(t._1, t._2) }
    }

  }

  @command(name = "help", description = "show help infomation of one command or all commands")
  object Help extends FirstArgumentCompleter {

    def apply(@argument(name = "command", description = "command name") command: String = "") {
      command match {
        case "" => list()
        case _  => usage(command)
      }
    }

    protected def allCandidates = commands

    private[this] def list() {
      // TODO
    }

    private[this] def usage(command: String) {
      // TODO
    }

  }

  @command(name = "quit", description = "quit the console")
  object Quit {
    def apply() {}
  }

}


