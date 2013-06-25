package housemd

import java.lang.instrument.Instrumentation
import java.util.jar.{Manifest => JarManifest, JarFile, JarOutputStream}
import java.io.{FileOutputStream, File}
import java.util.zip.ZipEntry
import com.google.common.io.ByteStreams

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Foreman(conf: Map[String, String], inst: Instrumentation) extends (() => Unit) {


  def apply() {
    inst.appendToBootstrapClassLoaderSearch(globalJar)


  }

  private def globalJar = {
    val file = new File(sys.props("user.home") + "/.housemd-global.jar")
    val jout = new JarOutputStream(new FileOutputStream(file), new JarManifest())
    val name = "housemd/Global.class"
    val in = getClass.getResourceAsStream('/' + name)

    jout.putNextEntry(new ZipEntry(name))

    ByteStreams.copy(in, jout)

    jout.close()

    new JarFile(file)
  }
}
