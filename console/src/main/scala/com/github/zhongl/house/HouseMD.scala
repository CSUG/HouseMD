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

import com.sun.tools.attach._
import collection.JavaConversions._
import com.beust.jcommander.{ParameterException, JCommander}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object HouseMD {

  def main(args: Array[String]) {
  }

  private[house] def insideWith(args: Array[String]) {
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

  private[house] def attach(pid: String, agentJarPath: String, agentOptions: String) {
    val vm = VirtualMachine.attach(pid)

    import actors.Actor._

    val progressor = actor {
      loop {
        react {
          case "." =>
            print(".")
            Thread.sleep(1000L)
            self ! "."
          case _   => exit()
        }
      }
    }

    sys.addShutdownHook {
      vm.detach()
      println("Detached pid: " + vm.id)
    }

    println("Attached pid: " + vm.id)

    progressor ! "."
    vm.loadAgent(agentJarPath, agentOptions)
    progressor ! "exit"
  }


}

