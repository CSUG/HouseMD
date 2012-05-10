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

package com.github.zhongl.house.cli

import com.github.zhongl.house.logging.Loggable

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

abstract class Commands(commands: AnyRef*) extends Loggable {
  private[this] val list = commands.toList ::: Quit :: Help :: Nil

  private[this] val name2Command = {
    val map = scala.collection.mutable.Map.empty[String, Command]
    list foreach {instance =>
      commandMethodOf(instance) match {
        case None         => warn("Skip invalid command {}", instance)
        case Some(method) =>
          val name = instance.getClass.getAnnotation(classOf[annotation.Command]).name()
          map += (name -> new Command(name, instance))
      }
    }
    map.toMap
  }

  def execute(name: String, arguments: String*) {
    name2Command get name match {
      case None          => warn("Unknown command {}", name)
      case Some(command) => command.apply(arguments)
    }
  }

  private[this] def commandMethodOf(instance: AnyRef) = instance.getClass.getMethods find {_.getName == "apply"}

  class Command(name: String, instance: AnyRef) extends (Seq[String] => Unit) {

    def apply(argStrings: Seq[String]) {
      try invoke(parse(argStrings)) catch {
        case e: IllegalArgumentException => warn(e.getMessage); Help.apply(name)
        case e: Throwable                => error(e.getMessage); throw e
      }
    }

    private[this] def invoke(arguments: Seq[AnyRef]) {
      commandMethodOf(instance).get.invoke(instance, arguments: _*)
    }

    private[this] def parse(argStrings: Seq[String]): Seq[AnyRef] = {
      // TODO
      argStrings
    }

  }

  @command(name = "help", description = "show help infomation of one command or all commands")
  object Help {

    def apply(@argument(name = "command", description = "command name") command: String = "") {
      command match {
        case "" => list()
        case _  => usage(command)
      }
    }

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
