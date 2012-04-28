package com.github.zhongl.insider

import com.sun.tools.attach._
import collection.JavaConversions._
import com.beust.jcommander.{ParameterException, JCommander}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object HouseMD {

  def main(args: Array[String]) {
    try {
      insideWith(args)
    } catch {
      case e => Console.err.print(e.getMessage); sys.exit(-1)
    }
  }

  private[insider] def insideWith(args: Array[String]) {
    val commander = new JCommander()
    val argsObject: Args = new Args

    commander.setProgramName(sys.props.getOrElse("program.name", "house"))
    commander.addObject(argsObject)

    try {
      commander.parse(args.toArray: _*)
      if (argsObject.params.size() < 2) throw new ParameterException("Missing parameter")

      val pid :: methodRegexs = argsObject.params.toList
      val validator = new RegexValidator
      methodRegexs.foreach(validator.validate("", _))

      attach(pid, argsObject.agentJarPath, args.mkString(" "))
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

