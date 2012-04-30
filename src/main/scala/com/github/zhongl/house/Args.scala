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

  @Parameter(names = Array("-p", "--package"), description = "regex pattern for package filter", validateWith = classOf[RegexValidator])
  var packageFilter = ".+"

  @Parameter(names = Array("-l", "--loaded"), description = "regex pattern for loaded class filter", validateWith = classOf[RegexValidator])
  var loaded = ".+"

  @Parameter(names = Array("-o", "--output"), description = "output file pattern for diagnosis report")
  var output = new File("diagnosis.report").getAbsolutePath

  @Parameter(names = Array("-t", "--timeout"), description = "seconds for diagnosing last", validateWith = classOf[PositiveInteger])
  var timeout = 60

  @Parameter(names = Array("-c", "--max-probe-count"), description = "max probe count for diagnosing last", validateWith = classOf[PositiveInteger])
  var maxProbeCount = 1000

  @Parameter(names = Array("-i", "--inspect"), description = "class names seperated by comma for inspecting at invocation")
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