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

package com.github.zhongl.housemd

import akka.actor.{Props, ActorSystem}
import akka.actor.ActorDSL._
import com.typesafe.config.ConfigFactory
import java.lang.instrument.Instrumentation
import java.lang.reflect.Modifier._

/**
 * Doctor [[com.github.zhongl.housemd.Cameron]] usually diagnose patient with [[com.github.zhongl.housemd.House]].
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Cameron(port: String, inst: Instrumentation) {

  import Diagnosis._
  import IPhone._

  def diagnose() {
    val number = port.toInt
    val loader = getClass.getClassLoader
    val config = loadConfigFrom(loader)
    val system = ActorSystem("hospital", config, loader)

    actor(system)(new Act {
      whenStarting { context.actorOf(Props[IPhone]) ! Dial(number) }
      become { case Instruction(name, arguments) => sender ! perform(name, arguments) }
    })

    system.awaitTermination()
    cleanAllThreadLocalFieldsThoseClassLoadedBy(loader)
  }


  private def cleanAllThreadLocalFieldsThoseClassLoadedBy(loader: ClassLoader) {
    // clean all ThreadLocal fields belongs to class loaded from this classloader to avoid PermGen memory leak.
    for {
      c <- inst.getAllLoadedClasses if c.getClassLoader == loader
      f <- c.getDeclaredFields if classOf[ThreadLocal[_]].isAssignableFrom(f.getType) && isStatic(f.getModifiers)
    } {
      f.setAccessible(true)
      f.get(null).asInstanceOf[ThreadLocal[_]].remove()
    }
  }

  private def perform(name: String, arguments: Array[String]) = {
    val content = s"$name $arguments"
    println(content)
    Feedback(content)
  }

  private def loadConfigFrom(loader: ClassLoader) = {
    def load(name: String) = ConfigFactory.load(loader, name)

    val defaultConfig = load("default.conf")

    // TODO test it
    if (loader.getResource("housemd.conf") == null) defaultConfig
    else load("housemd.conf").withFallback(defaultConfig)
  }
}

