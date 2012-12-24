/*
 * Copyright 2013 zhongl
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

package com.github.zhongl.housemd.house

import com.sun.tools.attach.VirtualMachine
import com.github.zhongl.yascli.{PrintOut, Command, Application}
import com.github.zhongl.housemd.misc.Utils._
import com.github.zhongl.housemd.duck.Cameron
import management.ManagementFactory
import java.io.{FileInputStream, FileWriter, BufferedWriter, File}
import java.util.jar.{Attributes, JarInputStream}
import akka.actor.ActorDSL._
import akka.actor.{Props, ActorSystem}


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object House extends Command("housemd", "a runtime diagnosis tool of JVM.", PrintOut(System.out)) with Application {

  implicit private val string2Port = {
    value: String =>
      val p = value.toInt
      if (p > 1024 && p < 65536) p else throw new IllegalArgumentException(", it should be between 1025 and 65535")
  }

  implicit private val string2File = {
    value: String =>
      val file = new File(value)
      if (file.exists() && file.isFile) file else throw new IllegalArgumentException(", it should be an existed file")
  }

  private val port = option[Int]("-p" :: "--port" :: Nil, "set console local socket server port number.", 54321)
  private val pid  = parameter[String]("pid", "id of process to be diagnosing.")

  private val printVersion = flag("-v" :: "--version" :: Nil, "show version.")


  private lazy val agentJarFile = sourceOf(Manifest.classType(getClass))
  private lazy val agentOptions = (new File(agentJarFile)).getParent :: classNameOf[Cameron] :: port() :: Nil

  private lazy val errorDetailFile   = "/tmp/housemd.err." + pid()
  private lazy val errorDetailWriter = new BufferedWriter(new FileWriter(errorDetailFile))

  def run() {
    if (printVersion()) {
      println("v" + version)
      return
    }

    if (ManagementFactory.getOperatingSystemMXBean.getName.toLowerCase.contains("window")) {
      throw new IllegalStateException("Sorry, Windows is not supported now.")
    }

    implicit val system = ActorSystem("console")
    try {
      val vm = VirtualMachine.attach(pid())

      system.actorOf(Props(new IPhone4s(port(), actor("House")(new Act {
        become {
          case msg: String => println(s"received: $msg")
        }
      }))))

      info("Welcome to HouseMD " + version)
      info(s"loading $agentJarFile with $agentOptions")

      vm.loadAgent(agentJarFile, agentOptions mkString " ")
      vm.detach()

    } catch {
      case e: Throwable =>
        system.shutdown()
        error(e)
        silentClose(errorDetailWriter)
    }
  }

  override def help = "version: " + version + '\n' + super.help

  private lazy val version = {
    val stream = new JarInputStream(new FileInputStream(agentJarFile))
    try {
      val attributes = stream.getManifest.getMainAttributes
      attributes.getValue(Attributes.Name.SIGNATURE_VERSION)
    } finally {
      silentClose(stream)
    }
  }

  override def error(a: Any) {
    super.error(a)
    a match {
      case throwable: Throwable =>
        super.error("You can get more details in " + errorDetailFile)
        throwable.getStackTrace foreach { s => errorDetailWriter.write(s + "\n") }
      case _                    =>
    }
  }

}


