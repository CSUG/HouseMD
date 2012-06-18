package com.github.zhongl.housemd.instrument

trait Hook {
  def enterWith(context: Context)

  def exitWith(context: Context)

  def heartbeat(now: Long)

  def end(throwable: Option[Throwable])
}
