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

  def listVMsAndPrintUsage() {
    val vmds = VirtualMachine.list
    println("""Usage: CMD <pid>
              |Please choose one JVM's pid detected below:""".stripMargin)
    VirtualMachine.list.foreach { vmd => println(format(vmd.id, vmd.displayName)) }
  }

  def format(id: String, name: String) = "\t%1$s\t%2$s".format(id, name)

  def attach(pid: String) {
    val vm = VirtualMachine.attach(pid)
    println(vm.id)
    vm.detach()
  }
}
