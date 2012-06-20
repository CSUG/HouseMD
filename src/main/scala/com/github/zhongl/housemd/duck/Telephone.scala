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

package com.github.zhongl.housemd.duck

import instrument.Instrumentation
import java.net.Socket
import jline.console.ConsoleReader
import com.github.zhongl.yascli.{Shell, PrintOut, Command}
import jline.TerminalFactory
import jline.console.history.FileHistory
import management.ManagementFactory
import java.io.File

/**
 * Telephone is used by Duck to communicate with House$.
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Telephone(inst: Instrumentation, port: Int, classes: Array[Class[Command]]) extends Runnable {

  def run() {
    val socket = new Socket("localhost", port)
    val reader = new ConsoleReader(socket.getInputStream, socket.getOutputStream)
    val name = ManagementFactory.getRuntimeMXBean.getName
    val history = new FileHistory(new File("/tmp/housemd/" + name + "/.history"))
    reader.setHistory(history)

    try {
      new Shell(name = "housemd", description = "a runtime diagnosis tool of jvm.", reader = reader) {
        override protected def commands = Quit :: helpCommand :: classes.map {toCommand(_, PrintOut(reader))}.toList

        override def error(a: Any) {
          super.error(a)
          if (a.isInstanceOf[Throwable]) {
            a.asInstanceOf[Throwable].getStackTrace foreach { s => println("\t" + s) }
          }
        }

      } main (Array.empty[String])
    } finally {
      TerminalFactory.reset()
      history.flush()
      socket.shutdownOutput()
      socket.shutdownInput()
      socket.close()
    }
  }

  private def toCommand(c: Class[Command], out: PrintOut) = {
    val I = classOf[Instrumentation]
    val O = classOf[PrintOut]
    val constructors = c.getConstructors
    val args = constructors(0).getParameterTypes map {
      case I => inst
      case O => out
      case x => throw new IllegalStateException("Unsupport parameter type: " + x)
    }
    constructors(0).newInstance(args: _*).asInstanceOf[Command]
  }

}

