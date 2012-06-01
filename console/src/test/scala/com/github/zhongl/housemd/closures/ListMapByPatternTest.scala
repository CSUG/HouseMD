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

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import collection.mutable.ListBuffer

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class ListMapByPatternTest extends FunSuite with ShouldMatchers {
  test("Enviroment") {
    val lines = ListBuffer.empty[String]
    val enviroment = new Enviroment("PATH")
    enviroment(null) {lines += _}
    lines.toList should contain("PATH = " + sys.env("PATH"))
  }

  test("Properties") {
    val lines = ListBuffer.empty[String]
    val properties = new Properites("os")
    properties(null) {lines += _}
    lines.toList should contain("os.name = " + sys.props("os.name"))
  }

}


