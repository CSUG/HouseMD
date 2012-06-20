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

package com.github.zhongl.housemd.house

import java.net.InetSocketAddress
import java.io.{IOException, OutputStream, InputStream}
import java.nio.channels._
import annotation.tailrec
import collection.JavaConversions._
import actors.Actor._
import com.github.zhongl.housemd.misc.Utils._
import java.nio.ByteBuffer
import actors.{Actor, OutputChannel, TIMEOUT}


/**
 * Mobilephone is used by House$ to communicate with Duck
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Mobilephone(port: Int, handle: PartialFunction[Signal, Any]) extends Actor {

  private val server   = ServerSocketChannel.open()
  private val selector = Selector.open()

  server.configureBlocking(false)
  server.socket().bind(new InetSocketAddress(port))
  server.register(selector, SelectionKey.OP_ACCEPT)

  def act() {
    val a = self
    val hook = sys.addShutdownHook {a !? PowerOff}
    var killer = Option.empty[OutputChannel[Any]]

    def select() {
      val selected = selector.select(500L)
      if (selected > 0) {
        selector.selectedKeys() foreach {
          case k if k.isAcceptable => accept(k, selector)
          case k if k.isReadable   => read(k)
          case k if k.isWritable   => if (killer.isEmpty) write(k) else sendExit(k)
          case ignore              =>
        }
        selector.selectedKeys().clear()
      }
    }

    def endCall() {
      if (killer.isEmpty) hook.remove()
      self ! EndCall
    }

    loop {
      reactWithin(1L) {
        case PowerOff => killer = Some(sender)
        case EndCall  =>
          handle(HangUp)
          killer foreach { o => o !() } // reply to killer for termination
          exit()
        case TIMEOUT  =>
          try {select()} catch {
            case Closed => endCall()
            case t      => handle(BreakOff(t)); hook.remove(); endCall()
          }
      }
    }
  }

  private def sendExit(key: SelectionKey) {
    write(key.channel(), "quit\n".getBytes)
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
      handle(PickUp)
    } catch {
      case e: IOException => silentClose(channel); throw e
    }
    uninterestOps(SelectionKey.OP_ACCEPT, key)
  }

  private def read(key: SelectionKey) {
    val channel = key.channel().asInstanceOf[ReadableByteChannel]

    @tailrec
    def output(stream: OutputStream) /*: Unit =*/ {
      val bytes = new Array[Byte](4096)
      val read = channel.read(ByteBuffer.wrap(bytes))
      if (read == -1) {
        silentClose(channel)
        throw Closed
      }
      stream.write(bytes, 0, read)
      if (read == bytes.length) output(stream)
    }

    handle(ListenTo(output))
    interestOps(SelectionKey.OP_READ, key)
  }

  private def write(key: SelectionKey) {

    def input(stream: InputStream) {
      val available = stream.available()
      if (available > 0) {
        val channel = key.channel()
        val bytes = new Array[Byte](available)

        stream.read(bytes)
        write(channel, bytes)
      }
    }

    handle(SpeakTo(input))
    interestOps(SelectionKey.OP_WRITE, key)
  }

  private def write(channel: Channel, bytes: Array[Byte]) {
    val write = channel.asInstanceOf[WritableByteChannel].write(ByteBuffer.wrap(bytes))
    if (write < bytes.length)
      throw new IllegalStateException("Can't send all input, you should enlarge socket send buffer.")
  }

  private def interestOps(op: Int, key: SelectionKey) = key.interestOps(key.interestOps() | op)

  private def uninterestOps(op: Int, key: SelectionKey) = key.interestOps(key.interestOps() & ~op)

  private case object EndCall

  private case object Closed extends Exception

}


