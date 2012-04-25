package com.github.zhongl.insider

import java.util.concurrent.TimeUnit._
import java.lang.System.{currentTimeMillis => now}
import java.util.concurrent.atomic.AtomicInteger
import scala.None
import java.util.concurrent.LinkedBlockingQueue
import java.lang.reflect.Method
import instrument.{ClassFileTransformer, Instrumentation}
import java.security.ProtectionDomain
import java.io.{InputStream, ByteArrayOutputStream, FileNotFoundException}
import scala.Predef._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Trace[+T](
                 private val inst: Instrumentation,
                 private val methodRegexs: Traversable[String],
                 private val timeout: Int,
                 private val maxCount: Int)
  extends AbstractIterator[T] {

  private[this] lazy val started        = now
  private[this] lazy val queue          = new LinkedBlockingQueue[T]
  private[this] lazy val count          = new AtomicInteger()
  private[this] lazy val toProbeClasses = inst.getAllLoadedClasses.filter(toProbe)

  private[this] lazy val probeTransformer = new ClassFileTransformer {
    def transform(
                   loader: ClassLoader,
                   className: String,
                   classBeingRedefined: Class[_],
                   protectionDomain: ProtectionDomain,
                   classfileBuffer: Array[Byte]) = {
      var bytes = classfileBuffer
      try {
        bytes = ClassDecorator.decorate(classfileBuffer, methodRegexs)
        // TODO LOGGER.info(format("probe class {1} from {0}", loader, className))
      } catch {
        case e: Exception => {
          // TODO LOGGER.info(format("transfor class {1} from {0}", loader, className))
        }
      }
      bytes
    }
  }

  private[this] lazy val resetTransformer = new ClassFileTransformer {
    def transform(
                   loader: ClassLoader,
                   className: String,
                   classBeingRedefined: Class[_],
                   protectionDomain: ProtectionDomain,
                   classfileBuffer: Array[Byte]) = {
      var bytes = classfileBuffer
      try {
        bytes = toBytes(loader.getResourceAsStream(className + ".class"))
        // TODO log "reset class {1} from {0}", loader, className
      } catch {
        case e =>
        // TODO log "transfor but not reset class {1} from {0}", loader, className
      }
      bytes
    }

    private[this] def toBytes(stream: InputStream): Array[Byte] = {
      if (stream == null) throw new FileNotFoundException
      val bytes = new ByteArrayOutputStream
      var read = stream.read
      while (read > -1) {
        bytes.write(read)
        read = stream.read
      }
      bytes.toByteArray
    }
  }

  // init code
  probe()

  protected def computeNext(): Option[T] = {
    if (isTimeout || isOverCount) {
      reset()
      None
    } else {
      val next = queue.poll(500L, MILLISECONDS)
      if (next == null) computeNext() else Some(next)
    }
  }

  private[this] def toProbe(method: Method) = !methodRegexs.find(method.getName.matches).isEmpty

  private[this] def toProbe(klass: Class[_]) = {
    val methods = (klass.getDeclaredMethods ++ klass.getMethods).toSet
    !methods.find(toProbe).isEmpty
  }

  private[this] def probe() {
    inst.addTransformer(probeTransformer)
    try {
      inst.retransformClasses(toProbeClasses: _*)
    } finally {
      inst.removeTransformer(probeTransformer)
    }
  }

  private[this] def reset() {
    queue.clear()
    inst.addTransformer(resetTransformer)
    try {
      inst.retransformClasses(toProbeClasses: _*)
    } finally {
      inst.removeTransformer(resetTransformer)
    }
  }

  private[this] def isOverCount = count.incrementAndGet() > maxCount

  private[this] def isTimeout = now - started >= SECONDS.toMillis(timeout)

}

