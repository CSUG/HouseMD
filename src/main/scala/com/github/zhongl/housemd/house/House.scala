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

package com.github.zhongl.housemd.house

import com.sun.tools.attach.VirtualMachine
import com.github.zhongl.yascli.{PrintOut, Command, Application}
import jline.NoInterruptUnixTerminal
import com.github.zhongl.housemd._
import command._
import misc.Utils._
import duck.Telephone
import management.ManagementFactory
import java.io.{FileInputStream, FileWriter, BufferedWriter, File}
import java.util.jar.{Attributes, JarInputStream}


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object House extends Command("housemd", "a runtime diagnosis tool of JVM.", PrintOut(System.out)) with Application {

  implicit private val string2Port = { value: String =>
    val p = value.toInt
    if (p > 1024 && p < 65536) p else throw new IllegalArgumentException(", it should be between 1025 and 65535")
  }

  implicit private val string2File = { value: String =>
    val file = new File(value)
    if (file.exists() && file.isFile) file else throw new IllegalArgumentException(", it should be an existed file")
  }

  private val port = option[Int]("-p" :: "--port" :: Nil, "set console local socket server port number.", 54321)
  private val pid  = parameter[String]("pid", "id of process to be diagnosing.")


  private lazy val agentJarFile = sourceOf(getClass)
  private lazy val agentOptions = agentJarFile ::
    classNameOf[Telephone] ::
    port() ::
    classNameOf[Trace] ::
    classNameOf[Loaded] ::
    classNameOf[Env] ::
    classNameOf[Inspect] ::
    classNameOf[Prop] ::
    Nil

  private lazy val errorDetailFile   = "/tmp/housemd.err." + pid()
  private lazy val errorDetailWriter = new BufferedWriter(new FileWriter(errorDetailFile))

  def run() {
    if (ManagementFactory.getOperatingSystemMXBean.getName.toLowerCase.contains("window")) {
      throw new IllegalStateException("Sorry, Windows is not supported now.")
    }
    try {
      val terminal = new NoInterruptUnixTerminal()
      terminal.init()
      val sout = terminal.wrapOutIfNeeded(System.out)
      val sin = terminal.wrapInIfNeeded(System.in)
      val vm = VirtualMachine.attach(pid())

      val mobilephone = new Mobilephone(port(), {
        case PickUp              => info("connection established on " + port())
        case ListenTo(earphone)  => earphone(sout)
        case SpeakTo(microphone) => microphone(sin)
        case BreakOff(reason)    => error("connection breaked causeby"); error(reason)
        case HangUp              => terminal.restore(); silentClose(errorDetailWriter); info("bye")

      })

      info("Welcome to HouseMD " + version)

      vm.loadAgent(agentJarFile, agentOptions mkString (" "))
      vm.detach()

      mobilephone.start()
    } catch {
      case e => error(e); silentClose(errorDetailWriter)
    }
  }

  private def version = {
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
    if (a.isInstanceOf[Throwable]) {
      super.error("You can get more details in " + errorDetailFile)
      a.asInstanceOf[Throwable].getStackTrace foreach { s => errorDetailWriter.write(s + "\n") }
    }
  }

}


