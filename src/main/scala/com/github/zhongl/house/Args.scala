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

package com.github.zhongl.house

import com.beust.jcommander.validators.PositiveInteger
import java.io.File
import com.beust.jcommander.{ParameterException, IParameterValidator, Parameter}
import java.util.regex.PatternSyntaxException

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Args {
  @Parameter(description = "<pid> <method regex> [more method regex...]")
  var params: java.util.List[String] = null

  @Parameter(hidden = true,names = Array("-p", "--package"), description = "regex pattern for package filter", validateWith = classOf[RegexValidator])
  var packageFilter = ".+"

  @Parameter(names = Array("-l", "--loaded"), description = "regex pattern for loaded class filter", validateWith = classOf[RegexValidator])
  var loaded = ".+"

  @Parameter(names = Array("-o", "--output"), description = "output file pattern for diagnosis report")
  var output = new File("diagnosis.report").getAbsolutePath

  @Parameter(names = Array("-t", "--timeout"), description = "seconds for diagnosing last", validateWith = classOf[PositiveInteger])
  var timeout = 60

  @Parameter(names = Array("-c", "--max-probe-count"), description = "max probe count for diagnosing last", validateWith = classOf[PositiveInteger])
  var maxProbeCount = 1000

  @Parameter(hidden = true, names = Array("-i", "--inspect"), description = "class names seperated by comma for inspecting at invocation")
  var inspects: java.util.List[String] = null

  @Parameter(hidden = true, names = Array("-a", "--agent"), description = "file path agent jar ")
  var agentJarPath: String = Utils.sourceOf(getClass)
}

class RegexValidator extends IParameterValidator {
  def validate(name: String, value: String) {
    try {
      value.r
    } catch {
      case e: PatternSyntaxException => throw new ParameterException(e.getMessage)
    }
  }
}