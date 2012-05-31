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

import actors.Actor.{actor => fork, self}
import com.sun.tools.attach.VirtualMachine
import java.net.InetSocketAddress
import collection.JavaConversions._
import java.nio.ByteBuffer
import java.nio.channels._
import annotation.tailrec
import java.io.{IOException, File}
import com.github.zhongl.command.{PrintOut, Command, Application}
import actors.Actor
import Utils._


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object House extends Command("house", "a runtime diagnosis tool of JVM.", PrintOut(System.out)) with Application {

  implicit private val string2Port = { value: String =>
    val p = value.toInt
    if (p > 1024 && p < 65536) p else throw new IllegalArgumentException(", it should be between 1025 and 65535")
  }

  implicit private val string2File = { value: String =>
    val file = new File(value)
    if (file.exists() && file.isFile) file else throw new IllegalArgumentException(", it should be an existed file")
  }

  private val port = option[Int]("-p" :: "--port" :: Nil, "set console local socket server port number.", 54321)
  private val pid  = parameter[String]("pid", "id of process to be diagnosing.")

  private lazy val agentJarFile = sourceOf(getClass)
  private lazy val agentOptions = port() :: classNameOf[Duck] :: classNameOf[Trace] :: Nil

  def run() {
    val server = ServerSocketChannel.open()
    val selector = Selector.open()

    try {
      server.configureBlocking(false)
      server.socket().bind(new InetSocketAddress(port()))
      info("bound localhost socket at " + port())
      server.register(selector, SelectionKey.OP_ACCEPT)

      val actor = fork {loop(selector)}

      registerCtrlCHandler(actor)

      driveAgentLoading()
    } catch {
      case e => error(e.toString)
    }

    silentClose(selector)
    silentClose(server)
    info("bye")
  }

  private def registerCtrlCHandler(actor: Actor) {
    val main = Thread.currentThread()

    @tailrec
    def waitForAgentExit() {
      try {main.join(5000L)} catch {
        case e: InterruptedException =>
          println("Something goes wrong, agent couldn't exit normally, you may wait more 5 seconds, or force kill it.")
          waitForAgentExit()
      }
    }

    sys.addShutdownHook {
      actor ! "exit"
      waitForAgentExit()
    }
  }

  private def silentClose(closable: {def close()}) {
    if (closable != null) try {closable.close()} catch {case _ => /*ignore*/ }
  }

  private def accept(key: SelectionKey, selector: Selector) {
    val channel = key.channel().asInstanceOf[ServerSocketChannel].accept()
    if (channel == null) return
    try {
      channel.socket().setSoLinger(true, 1)
      channel.socket().setTcpNoDelay(true)
      channel.socket().setSendBufferSize(1024)
      channel.configureBlocking(false)
      channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE)
    } catch {
      case e: IOException => silentClose(channel); throw e
    }
    key.interestOps(key.interestOps() & ~SelectionKey.OP_ACCEPT)
  }

  private def read(key: SelectionKey) {
    val channel = key.channel().asInstanceOf[ReadableByteChannel]

    @tailrec
    def output() {
      val bytes = new Array[Byte](4096)
      val read = channel.read(ByteBuffer.wrap(bytes))
      if (read == -1) {
        silentClose(channel)
        throw new ExitException()
      }
      System.out.write(bytes, 0, read)
      if (read == bytes.length) output()
    }

    output()
  }

  private def write(key: SelectionKey) {
    val available = System.in.available()

    if (available > 0) {
      val channel = key.channel()
      val bytes = new Array[Byte](available)

      System.in.read(bytes)
      write(channel, bytes)
    }
  }

  private def write(channel: Channel, bytes: Array[Byte]) {
    val write = channel.asInstanceOf[WritableByteChannel].write(ByteBuffer.wrap(bytes))
    if (write < bytes.length)
      throw new IllegalStateException("Can't send all input, you should enlarge socket send buffer.")
  }

  private def loop(selector: Selector) {
    def sendQuitTo(c: Channel) {
      write(c, "quit\n".getBytes)
    }

    var quit = false

    while (true) {
      val selected = selector.select(500L)

      if (selected > 0) {
        selector.selectedKeys() foreach {
          case k if k.isAcceptable => accept(k, selector)
          case k if k.isReadable   => read(k)
          case k if k.isWritable   => if (quit) sendQuitTo(k.channel()) else write(k)
          case _                   => //ignore
        }
      }

      self.receiveWithin(10L) {
        case "exit" => quit = true
      }
    }
  }

  private def driveAgentLoading() {
    val vm = VirtualMachine.attach(pid())
    info("attached vm " + pid())
    try {
      vm.loadAgent(agentJarFile, agentOptions mkString (" "))
      info("loaded agent " + agentJarFile + ", options: " + agentOptions)
    } catch {
      case e => error(e.toString)
    } finally {
      vm.detach()
      info("detached vm " + pid())
    }
  }

  class ExitException extends Exception
}
