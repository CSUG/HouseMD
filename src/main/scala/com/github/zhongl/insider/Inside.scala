package com.github.zhongl.insider

import com.sun.tools.attach._
import collection.JavaConversions._
import com.beust.jcommander.{ParameterException, JCommander}

object Inside {

  def main(args: Array[String]) {
    try {
      driveWith(args)
    } catch {
      case e => sys.error(e.getMessage); sys.exit(-1)
    }
  }

  private[insider] def driveWith(args: Array[String]) {
    val commander = new JCommander()
    val argsObject: Args = new Args

    commander.setProgramName("house")
    commander.addObject(argsObject)

    try {
      commander.parse(args.toArray: _*)
      if (argsObject.params.size() < 2) throw new ParameterException("Missing parameter")

      val pid :: methodRegexs = argsObject.params.toList
      val validator = new RegexValidator
      methodRegexs.foreach(validator.validate("", _))

      attach(pid, argsObject.agentJarPath, args.foldLeft("")(_ + " " + _))
    } catch {
      case e =>
        val sb = new java.lang.StringBuilder()
        sb.append(e.getClass.getSimpleName).append(": ").append(e.getMessage).append('\n')
        commander.usage(sb)
        throw new RuntimeException(sb.toString)
    }

  }

  private[insider] def attach(pid: String, agentJarPath:String, agentOptions:String) {
    val vm = VirtualMachine.attach(pid)
    println("Attached pid: " + vm.id)
    vm.loadAgent(agentJarPath,agentOptions)
    vm.detach()
    println("Detached pid: " + vm.id)
  }
}

