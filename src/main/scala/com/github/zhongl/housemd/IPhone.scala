package com.github.zhongl.housemd

import akka.actor.{IO, ActorRef, IOManager, Actor}
import akka.serialization.SerializationExtension
import java.io.Serializable
import java.net.InetSocketAddress
import akka.util.ByteString

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
class IPhone extends Actor {

  private val state                     = IO.IterateeRef.async()(context.dispatcher)
  private val manager                   = IOManager(context.system)
  private val serializer                = SerializationExtension(context.system).serializerFor(classOf[Serializable])
  private var opposite: IO.SocketHandle = _
  private var user    : ActorRef        = _

  import IPhone._
  import Diagnosis._

  def receive = {
    case IO.NewClient(server)          => state flatMap (_ => getThrough(server.accept()))
    case IO.Connected(socket, address) => state flatMap (_ => getThrough(socket))
    case IO.Read(socket, bytes)        => state(IO Chunk bytes)
    case IO.Closed(socket, cause)      => context.system.shutdown()
    case Dial(number)                  => manager connect local(number); user = sender
    case Standby(number)               => manager listen local(number); user = sender
    case Hangup                        => context.system.shutdown()
    case term: Term                    => opposite.write(encode(term))
  }

  private def local(number: Int) = new InetSocketAddress(number)

  private def getThrough(socket: IO.SocketHandle) = {
    opposite = socket
    user ! GetThrough
    decode
  }

  private def decode = IO repeat {
    for {
      length <- IO take 4 map toInt
      content <- IO take length map toArray
    } yield user ! (serializer fromBinary content)
  }

  private def encode(value: AnyRef) = {
    val content = serializer.toBinary(value)
    ByteString.newBuilder.putInt(content.length).putBytes(content).result()
  }

  implicit val byteOrder = java.nio.ByteOrder.BIG_ENDIAN

  private def toInt(bs: ByteString) = bs.iterator.getInt

  private def toArray(bs: ByteString) = bs.toArray
}

object IPhone {

  case class Dial(number: Int)

  case class Standby(number: Int)

  case object Hangup

  case object GetThrough

}

