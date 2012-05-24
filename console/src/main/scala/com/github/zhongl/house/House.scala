package com.github.zhongl.house

import com.github.zhongl.CommandLineApplication
import java.io.File


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */

object House extends CommandLineApplication {
  val name        = "house"
  val version     = "0.2.0"
  val description = "a runtime diagnosis tool of JVM."

  val debug = flag("-d" :: "--debug" :: Nil, "enable debug mode.")

  val port = option[Int]("-p" :: "--port" :: Nil, "set console local socket server port number.", 54321) { value: String =>
    val p = value.toInt
    if (p > 1024 && p < 65536) p else throw new IllegalArgumentException(", it should be between 1025 and 65535")
  }

  val agent = option[File]("-a" :: "--agent" :: Nil, "set java agent jar file.", new
      File("agent.jar")) { value: String =>
    val file = new File(value)
    if (file.exists() && file.isFile) file else throw new IllegalArgumentException(", it should be an existed file")
  }

  val pid = parameter[String]("pid", "id of process to be diagnosing.")

  protected def run() {}
}
