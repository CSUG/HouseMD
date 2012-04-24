package com.github.zhongl.insider

import java.lang.instrument._

object Diagnosis {

  private[insider] def probeWith(args: Array[String], inst: Instrumentation) {

  }

  def agentmain(agentArg: String, inst: Instrumentation) {
    probeWith(agentArg.split(" "), inst)
  }
  def premain(agentArg: String, inst: Instrumentation) {
    probeWith(agentArg.split(" "), inst)
  }
}
