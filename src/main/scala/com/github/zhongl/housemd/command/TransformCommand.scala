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

package com.github.zhongl.housemd.command

import instrument._
import java.lang.System.{currentTimeMillis => now}
import com.github.zhongl.yascli.{PrintOut, Command}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
abstract class TransformCommand(name:String, description:String, inst:Instrumentation, out:PrintOut)
  extends Command(name,description,out) {


//  protected lazy val advice = new Advice() {
//    val count = new AtomicInteger()
//    val max   = limit()
//    val host  = self
//
//    def enterWith(context: Map[String, AnyRef]) {
//      host ! EnterWith(context)
//    }
//
//    def exitWith(context: Map[String, AnyRef]) {
//      host ! ExitWith(context)
//      if (count.incrementAndGet() >= max) host ! OverLimit
//    }
//  }
//
//  protected lazy val probeTransformer = new ClassFileTransformer {
//
//    def transform(
//      loader: ClassLoader,
//      className: String,
//      classBeingRedefined: Class[_],
//      protectionDomain: ProtectionDomain,
//      classfileBuffer: Array[Byte]) = {
//      try {
//        if (candidates.contains(classBeingRedefined))
//          ClassDecorator.decorate(classfileBuffer, methodFilters())
//        else null
//      } catch {
//        case x => x.printStackTrace(); error(x); null
//      }
//    }
//  }
//
//  protected lazy val candidates = {
//    val pp = packagePattern()
//    val mfs = methodFilters()
//
//    @inline
//    def packageOf(c: Class[_]): String = if (c.getPackage == null) "" else c.getPackage.getName
//
//    def isNotInterface(c: Class[_]): Boolean = if (c.isInterface) {warn("Skip " + c); false} else true
//
//    def isNotFromBootClassLoader(c: Class[_]) =
//      if (isFromBootClassLoader(c)) {warn("Skip " + c + " loaded from bootclassloader."); false} else true
//
//    def isNotHouseMD(c: Class[_]) = if (c.getName.startsWith("com.github.zhongl.housemd")) {
//      warn("Skip " + c + " belongs to HouseMD.")
//      false
//    } else true
//
//    inst.getAllLoadedClasses filter { c =>
//      pp.matcher(packageOf(c)).matches() &&
//      mfs.find(_.filter(c)).isDefined &&
//      isNotInterface(c) &&
//      isNotFromBootClassLoader(c) &&
//      isNotHouseMD(c)
//    }
//  }

  override def run() {
//    if (candidates.isEmpty) {
//      println("No matched class")
//    } else {
//      probe()
//      act()
//      reset()
//    }
  }

//  protected def hook: Hook
//
//  private def probe() {
//    inst.addTransformer(probeTransformer, true)
//    retransform("probe ")(_.getMethod(Advice.SET_DELEGATE, classOf[Object]).invoke(null, advice))
//    inst.removeTransformer(probeTransformer)
//  }
//
//  private def act() {
//    val start = now
//    val timoutMillis = timeout().toMillis
//    val h = hook
//
//    breakable {
//      while (true) {
//        receiveWithin(500) {
//          case TIMEOUT            => // ignore
//          case OverLimit          => info("Ended by overlimit"); break()
//          case EnterWith(context) => h.enterWith(context)
//          case ExitWith(context)  => h.exitWith(context)
//          case x                  => // ignore last unread messages, error("Unknown case: " + x)
//        }
//
//        val t = now
//        h.heartbeat(t)
//
//        if (t - start >= timoutMillis) {
//          info("Ended by timeout")
//          break()
//        }
//      }
//    }
//
//    h.end()
//  }
//
//  private def reset() {
//    retransform("reset ")(_.getMethod(Advice.SET_DEFAULT_DELEGATE).invoke(null))
//  }
//
//  private def retransform(action: String)(handle: Class[_] => Unit) {
//    candidates foreach { c =>
//      try {
//        handle(loadOrDefine(classOf[Advice], c.getClassLoader))
//        inst.retransformClasses(c)
//        info(action + c)
//      } catch {
//        case t: Throwable => warn("Failed to " + action + c + " because of " + t)
//      }
//    }
//  }
//
//  sealed trait Event
//
//  private case object OverLimit extends Event
//
//  case class EnterWith(context: Context) extends Event
//
//  case class ExitWith(context: Context) extends Event
//
//  trait Hook {
//    def enterWith(context: Context) {}
//
//    def exitWith(context: Context) {}
//
//    def heartbeat(now: Long) {}
//
//    def end() {}
//  }

}

