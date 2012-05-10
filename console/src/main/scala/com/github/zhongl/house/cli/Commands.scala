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

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

class Commands(commands: AnyRef*) {
  private[this] val list = commands.toList ::: Quit :: Help :: Nil

  private[this] lazy val name2Command = {
    val map = scala.collection.mutable.Map.empty[String, Array[AnyRef] => Unit]
    list foreach {instance =>
      commandMethodOf(instance) match {
        case None         => Unit
        case Some(method) =>
          val name = instance.getClass.getAnnotation(classOf[annotation.Command]).name()
          map += (name -> {args: Array[AnyRef] => method.invoke(instance, args: _*) })
      }
    }
    map.toMap
  }

  private[this] def commandMethodOf(instance: AnyRef) = instance.getClass.getMethods find {_.getName == "apply"}

  @throws(classOf[IllegalArgumentException])
  def command(name: String): (Array[AnyRef]) => Unit = name2Command get name match {
    case None => throw new IllegalArgumentException("Unknown command: " + name)
    case Some(command) => command
  }

  @command(name = "help", description = "show help infomation of the command or all commands")
  object Help {

    def apply(@argument(name = "command", description = "command name to show information") command: String = "*") {
      command match {
        case "*" => list()
        case _   => usage(command)
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




