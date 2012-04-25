package com.github.zhongl.insider

import java.lang.instrument._
import java.lang.System.{currentTimeMillis => now}
import java.io._
import java.util.Date
import com.beust.jcommander.{ParameterException, JCommander}
import scala.collection.JavaConversions._
import management.ManagementFactory
import java.util.concurrent.TimeUnit._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
object Diagnosis {

  private[insider] def probeWith(agentOptions: String, inst: Instrumentation) {
    val args = parse(agentOptions.split(" "))

    reportTo(args.output) {
      implicit stream: PrintStream =>

        stream.printf("#Diagnosis report\n> created at %tc\n\n", new Date)

        Section("Summary") {
          val runtime = ManagementFactory.getRuntimeMXBean

          val pairs = ("name = " + runtime.getName) ::
            ("arguments = " + runtime.getInputArguments) ::
            ("starTime = %tc" format new Date(runtime.getStartTime)) ::
            ("upTime = %1$d hours %2$d minutes %3$d seconds" format (convert(runtime.getUptime): _*)) ::
            Nil

          pairs.toIterator
        }

        Section("Enviroment") {
          list(sys.env).toIterator
        }

        Section("Properties") {
          list(sys.props.toMap).toIterator
        }

        Section("Loaded classes: " + args.loaded) {
          inst.getAllLoadedClasses.filter(_.getName.matches(args.loaded)).map {
            c: java.lang.Class[_] =>

              val name: String = c.getName
              val path: String = '/' + name.replace('.', '/') + ".class"
              name + " -> " + c.getResource(path).toString
          }.toIterator
        }

        val methodRegexs = args.params.tail
        // TODO do probe
        Section("Traces: " + methodRegexs) {
          new Trace(inst, methodRegexs, args.timeout, args.maxProbeCount)
        }
    }
  }

  def parse(args: Array[String]) = {
    val argsObject = new Args
    val commander = new JCommander()
    commander.addObject(argsObject)

    try {
      commander.parse(args.toArray: _*)
      if (argsObject.params.size() < 2) throw new ParameterException("Missing parameter")

      val _ :: methodRegexs = argsObject.params.toList
      methodRegexs.foreach((new RegexValidator).validate("", _))

      argsObject
    } catch {
      case e =>
        val sb = new java.lang.StringBuilder()
        sb.append(e.getClass.getSimpleName).append(": ").append(e.getMessage).append('\n')
        commander.usage(sb)
        throw new RuntimeException(sb.toString)
    }
  }

  private[this] def convert(l: Long) = {
    var r = l
    val hours = MILLISECONDS.toHours(r)
    r -= HOURS.toMillis(hours)
    val minutes = MILLISECONDS.toMinutes(r)
    r -= MINUTES.toMillis(minutes)
    val seconds = MILLISECONDS.toMinutes(r)

    Array(hours, minutes, seconds)
  }

  private[this] def list(kv: Map[String, String]) = kv map {
    case (k, v) => k + " = " + v
  }

  private[this] def reportTo(path: String)(appending: PrintStream => Unit) {
    mkdirsFor(path)
    val stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(path, true)))
    try {
      appending(stream)
    } catch {
      case e => e.printStackTrace(stream)
    } finally {
      stream.flush()
      stream.close()
    }
  }

  private[this] def mkdirsFor(path: String) = new File(path).getParentFile.mkdirs()

  def agentmain(agentOptions: String, inst: Instrumentation) {
    probeWith(agentOptions, inst)
  }

  def premain(agentOptions: String, inst: Instrumentation) {
    probeWith(agentOptions, inst)
  }
}

