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

package com.github.zhongl.house

import instrument.Instrumentation
import java.nio.channels.SocketChannel
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import actors.Actor

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
abstract class ClosureExecutor(consoleAddress: String, inst: Instrumentation) extends Runnable {

  private lazy val in = ByteBuffer.allocate(1024)

  protected def parse(consoleAddress: String): (String, Int)

  protected def readClosureNameFrom(channel: SocketChannel): String

  def run() {
    val (host, port) = parse(consoleAddress)
    val channel = SocketChannel.open(new InetSocketAddress(host, port))
    val engine = new Engine(channel)
    try {
      val name = readClosureNameFrom(channel)
//      closure(name).executeWith(inst, engine)
    } catch {
      case e: Throwable => // notify console
    } finally {
      engine ! Exit
    }
  }

  class Engine(channel: SocketChannel) extends Actor {
    def act() {
      loop {
        react {
          case Exit => exit()
          case unknown => exit(unknown.toString)
        }

      }
    }

//    override protected[actors] def exit() = {
//      silentClose(channel)
//      super.exit()
//    }
  }

  case class Exit()

  def silentClose(closable: {def close(): Unit}) {
    try {
      closable.close()
    } catch {
      case _ => Unit
    }
  }
}
