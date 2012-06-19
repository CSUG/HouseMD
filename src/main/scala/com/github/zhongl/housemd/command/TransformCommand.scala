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
import com.github.zhongl.yascli.{PrintOut, Command}
import com.github.zhongl.housemd.instrument._
import java.util.regex.Pattern

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
abstract class TransformCommand(name: String, description: String, inst: Instrumentation, out: PrintOut)
  extends Command(name, description, out) {

  import com.github.zhongl.yascli.Converters._

  private val packagePattern = option[Pattern]("-p" :: "--package" :: Nil, "package regex pattern for filtering.", ".*")
  private val interval       = option[Seconds]("-i" :: "--interval" :: Nil, "display trace statistics interval.", 1)
  private val timeout        = option[Seconds]("-t" :: "--timeout" :: Nil, "limited trace seconds.", 10)
  private val overLimit      = option[Int]("-l" :: "--limit" :: Nil, "limited limited times.", 1000)

  private val transform = new Transform

  override def run() {
    val delegate = hook
    val h = new Hook {
      val intervalMillis = interval().toMillis
      var last           = 0L

      def heartbeat(now: Long) {
        if (now - last > intervalMillis) {
          delegate.heartbeat(now)
          last = now
        }
      }

      def finalize(throwable: Option[Throwable]) {
        delegate.finalize(throwable)
      }

      def enterWith(context: Context) {
        delegate.enterWith(context)
      }

      def exitWith(context: Context) {
        delegate.exitWith(context)
      }
    }

    transform(inst, filter, timeout(), overLimit(), this, h)
  }

  protected def isCandidate(klass: Class[_]): Boolean

  protected def isDecorating(klass: Class[_], methodName: String): Boolean

  protected def hook: Hook

  private def filter = new Filter {
    // Used for getting candidates of probing
    def apply(klass: Class[_]) = {
      @inline
      def matchesPackagePattern = {
        val p = packagePattern()
        p.pattern() == ".*" || (klass.getPackage != null && p.matcher(klass.getPackage.getName).matches())
      }

      matchesPackagePattern && isCandidate(klass)
    }

    // Used for class decorating
    def apply(klass: Class[_], methodName: String) = isDecorating(klass, methodName)
  }

}

