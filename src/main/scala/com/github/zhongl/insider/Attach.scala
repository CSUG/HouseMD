package com.github.zhongl.insider

import com.sun.tools.attach._
import scala.collection.JavaConversions._

object Attach {
  private val Index = """(\d+)""".r

  def main(args: Array[String]) {
    args match {
      case Array(pid) => attach(pid)
      case _ => listVMsAndPrintUsage()
    }
  }

  private[insider] def listVMsAndPrintUsage() {
    val vmds = VirtualMachine.list
    println("""Usage: CMD <pid>
              |Please choose one JVM's pid detected below:""".stripMargin)
    VirtualMachine.list.foreach { vmd => println(format(vmd.id, vmd.displayName)) }
  }

  private[insider] def format(id: String, name: String) = "\t%1$s\t%2$s".format(id, name)

  private[insider] def attach(pid: String) {
    val vm = VirtualMachine.attach(pid)
    println("Attached pid: " + vm.id)

    vm.detach()
    println("Detached pid: " + vm.id)
  }
}
