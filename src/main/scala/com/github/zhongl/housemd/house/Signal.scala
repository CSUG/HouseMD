package com.github.zhongl.housemd.house

import java.io.{InputStream, OutputStream}

sealed trait Signal

case object PickUp extends Signal

case class ListenTo(earphone: OutputStream => Unit) extends Signal

case class SpeakTo(microphone: InputStream => Unit) extends Signal

case class BreakOff(reason: Throwable) extends Signal

case object HangUp extends Signal

case object PowerOff

