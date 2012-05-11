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

package com.github.zhongl.house.logging

import org.scalatest.Assertions

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

trait AssertLog extends Loggable with Assertions{

  import Level._

  var level  : Level       = _
  var pattern: String      = _
  var anyRefs: Seq[AnyRef] = _

  override protected def debug(pattern: String, anyRefs: AnyRef*) {
    this.level = Debug
    this.pattern = pattern
    this.anyRefs = anyRefs
  }

  override protected def info(pattern: String, anyRefs: AnyRef*) {
    this.level = Info
    this.pattern = pattern
    this.anyRefs = anyRefs
  }

  override protected def warn(pattern: String, anyRefs: AnyRef*) {
    this.level = Warn
    this.pattern = pattern
    this.anyRefs = anyRefs
  }

  override protected def error(pattern: String, anyRefs: AnyRef*) {
    this.level = Error
    this.pattern = pattern
    this.anyRefs = anyRefs
  }

  def shouldLogged(level: Level, pattern: String, anyRefs: AnyRef*) {
    assert(this.level === level)
    assert(this.pattern === pattern)
    assert(this.anyRefs === anyRefs)
  }

  def shouldNotLogged() {
    assert(this.level === null)
    assert(this.pattern === null)
    assert(this.anyRefs === null)
  }
}
