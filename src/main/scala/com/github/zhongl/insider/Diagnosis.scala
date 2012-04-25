package com.github.zhongl.insider

import java.lang.instrument._
import java.io._
import com.beust.jcommander.{ParameterException, JCommander}
import scala.collection.JavaConversions._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object Diagnosis {

  private[insider] def probeWith(args: Array[String], inst: Instrumentation) {
    val commander = new JCommander()
    val argsObject = new Args

    commander.addObject(argsObject)

    try {
      commander.parse(args.toArray: _*)
      if (argsObject.params.size() < 2) throw new ParameterException("Missing parameter")

      val _ :: methodRegexs = argsObject.params.toList
      val validator = new RegexValidator
      methodRegexs.foreach(validator.validate("", _))

      reportTo(argsObject.output) {
        implicit stream: PrintStream =>
        // TODO         Section("Summary", ).render(stream)

          Section("Enviroment") {
            list(sys.env)
          }.render

          Section("Properties") {
            list(sys.props.toMap)
          }.render

        //          Section("Loaded classes", inst.getAllLoadedClasses.map {
        //            c: java.lang.Class[_] =>
        //              val name: String = c.getName
        //              val path: String = '/' + name.replace('.', '/') + ".class"
        //              name + "->" + c.getResource(path).toString
        //          }).render(stream)

        // TODO         Section("Traces", ).render(stream)

      }
    } catch {
      case e =>
        val sb = new java.lang.StringBuilder()
        sb.append(e.getClass.getSimpleName).append(": ").append(e.getMessage).append('\n')
        commander.usage(sb)
        throw new RuntimeException(sb.toString)
    }

  }


  private[this] def list(kv: Map[String, String]) = kv map {
    case (k, v) => k + "=" + v
  }

  private[this] def reportTo(path: String)(appending: PrintStream => Unit) {
    mkdirsFor(path)
    val stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(path, true)))
    try {
      appending(stream)
    } finally {
      stream.close()
    }
  }

  private[this] def mkdirsFor(path: String) {
    new File(path).getParentFile.mkdirs()
  }

  def agentmain(agentArg: String, inst: Instrumentation) {
    probeWith(agentArg.split(" "), inst)
  }

  def premain(agentArg: String, inst: Instrumentation) {
    probeWith(agentArg.split(" "), inst)
  }
}

