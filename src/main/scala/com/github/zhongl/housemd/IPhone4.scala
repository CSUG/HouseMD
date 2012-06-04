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

package com.github.zhongl.housemd

import Utils._
import java.net.InetSocketAddress
import actors.{TIMEOUT, Actor}
import annotation.tailrec
import java.nio.ByteBuffer
import java.nio.channels._
import java.io.{OutputStream, InputStream, IOException}
import collection.JavaConversions._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

/**
* IPhone4 is used by HouseMD to communicate with Duck.
*/
class IPhone4(port: Int, in: InputStream, out: OutputStream, error: Throwable => Unit) extends Actor {
  val server   = ServerSocketChannel.open()
  val selector = Selector.open()

  server.configureBlocking(false)
  server.socket().bind(new InetSocketAddress(port))
  server.register(selector, SelectionKey.OP_ACCEPT)

  def act() {

    def sendQuitTo(c: Channel) {
      write(c, "quit\n".getBytes)
    }

    var quit = false
    def select() {
      val selected = selector.select(500L)
      if (selected > 0) {
        selector.selectedKeys() foreach {
          case k if k.isAcceptable => accept(k, selector)
          case k if k.isReadable   => read(k)
          case k if k.isWritable   => if (quit) sendQuitTo(k.channel()) else write(k)
          case ignore              =>
        }
        selector.selectedKeys().clear()
      }
    }

    try {
      while (true) {
        select()

        receiveWithin(10L) {
          case Quit    => quit = true
          case Break   => try {select() /* read EOF */ } finally {reply()}
          case TIMEOUT => // ignore
        }
      }
    } catch {
      case ignore: ExitException =>
      case t: Throwable          => error(t)
    }

    silentClose(selector)
    silentClose(server)
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
      out.write(bytes, 0, read)
      if (read == bytes.length) output()
    }

    output()
    interestOps(SelectionKey.OP_READ, key)
  }

  private def write(key: SelectionKey) {
    val available = in.available()
    if (available > 0) {
      val channel = key.channel()
      val bytes = new Array[Byte](available)

      in.read(bytes)
      write(channel, bytes)
    }
    interestOps(SelectionKey.OP_WRITE, key)
  }

  private def interestOps(op: Int, key: SelectionKey) {
    key.interestOps(key.interestOps() | op)
  }

  private def write(channel: Channel, bytes: Array[Byte]) {
    val write = channel.asInstanceOf[WritableByteChannel].write(ByteBuffer.wrap(bytes))
    if (write < bytes.length)
      throw new IllegalStateException("Can't send all input, you should enlarge socket send buffer.")
  }

  class ExitException extends Exception

  case class Quit()

  case class Break()

}
