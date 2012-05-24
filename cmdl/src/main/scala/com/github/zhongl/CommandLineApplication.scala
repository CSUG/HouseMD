package com.github.zhongl

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

trait CommandLineApplication extends CommandLine {

  private val printHelp = flag("-h" :: "--help" :: Nil, "show help infomation of this command.")

  def main(arguments: Array[String]) {
    try {
      parse(arguments)
      if (printHelp()) print(help) else run()
    } catch {
      case UnknownOptionException(name)     => println("Unknown option: " + name)
      case MissingParameterException(name)  => println("Missing parameter: " + name)
      case ConvertingException(name, value) => println("Invalid " + name + " value: " + value)
    }
  }
}
