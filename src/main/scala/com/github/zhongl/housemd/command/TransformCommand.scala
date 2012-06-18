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
import com.github.zhongl.housemd.instrument.{Filter, Seconds, Hook, Transform}

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
abstract class TransformCommand(name: String, description: String, inst: Instrumentation, out: PrintOut)
  extends Command(name, description, out) {

  private val transform = new Transform

  override def run() {
    transform(inst, filter, timeout, overLimit, this, hook)
  }

  protected def hook: Hook

  protected def timeout: Seconds

  protected def overLimit: Int

  protected def filter: Filter
}

