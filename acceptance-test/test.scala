#!/bin/bash
exec java -Dsbt.main.class=sbt.ScriptMain -Dsbt.boot.directory=/tmp/.sbt/boot -jar ../sbt-launch.jar "$0" "$@"
!#

import management.ManagementFactory
import sys.process._
import actors.Actor._

object TraceTarget {
  def addOne(i: Int) = i + 1
}

val a = actor {
  loop {
    react {
      case "x" => exit()
      case i:Int =>
        Thread.sleep(10L)
        self ! TraceTarget.addOne(i)
    }
  }
}

a ! 1

val jarPath = sys.props("user.dir") + "/../target/insider-assembly-0.1-SNAPSHOT.jar"
val NameRE = """(\d+)@.+""".r
val NameRE(pid) = ManagementFactory.getRuntimeMXBean().getName()


"java -cp /usr/lib/jvm/java-6-sun/lib/tools.jar -jar " + jarPath + " -a " + jarPath + " -t 2 " + pid + " .+TraceTarget.+" !!

a ! "x"

println("test passed!")
