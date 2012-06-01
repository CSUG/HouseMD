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

package com.github.zhongl.housemd.closures

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

import instrument.Instrumentation

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object ClosureExecutor extends App {
  val klass     = Class.forName("com.github.zhongl.housemd.Summary").asInstanceOf[Class[Closure]]
  val method    = klass.getMethod("apply", classOf[Instrumentation], classOf[String => Unit])
  val instance  = klass.newInstance()
  val arguments = Seq(null, {x: String => println(x) }).asInstanceOf[Seq[AnyRef]]
  method invoke(instance, arguments: _*)

}

class a extends StaticAnnotation