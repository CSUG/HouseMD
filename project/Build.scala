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

import java.security.MessageDigest
import org.eclipse.egit.github.core.{Download, RepositoryId}
import sbt._
import sbt.Keys._
import java.io.File
import sbtassembly.Plugin._
import AssemblyKeys._
import org.eclipse.egit.github.core.client._
import org.eclipse.egit.github.core.service._

object Build extends sbt.Build {
  import Dependencies._
  import Unmanaged._

  val VERSION = "0.2.3"

  lazy val upload = TaskKey[Unit]("upload","upload assembly jar to github downloads")

  lazy val root = Project(
    id       = "housemd",
    base     = file("."),
    settings = Defaults.defaultSettings ++ classpathSettings ++ assemblySettings ++ Seq(
      name                := "housemd",
      organization        := "com.github.zhongl",
      version             := VERSION,
      scalaVersion        := "2.9.2",
      scalacOptions       ++= Seq("-unchecked", "-deprecation"),
      resolvers           += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
      libraryDependencies :=  compileLibs ++ testLibs,
      upload              <<= outputPath in assembly map { file => uploadToGithubWith(file)},
      packageOptions      +=  Package.ManifestAttributes(
        ("Main-Class","com.github.zhongl.housemd.house.House"),
        ("Agent-Class","com.github.zhongl.housemd.duck.Duck"),
        ("Can-Retransform-Classes","true"),
        ("Can-Redefine-Classes","true"),
        ("Signature-Version",VERSION)
      ),
      test in assembly := {},
      parallelExecution in Test := false,
      excludedJars in assembly <<= (fullClasspath in assembly) map { _ filter {_.data.getName == "tool.jar"} }
    )
  )

  private def uploadToGithubWith(file: File) {
    import collection.JavaConversions._

    val username = readInput("Please input credentials username: ")
    val password = readHidden("Please input credentials password: ")

    val client = new GitHubClient()
    client.setCredentials(username, password)

    val service = new DownloadService(client)
    val id = new RepositoryId("zhongl", "HouseMD")
    service.getDownloads(id) find {_.getName == file.getName} foreach {d => service.deleteDownload(id,d.getId)} // delete if existed
    service.createDownload(id, aDownloadOf(file), file)
  }

  private def sha1sum(file: File) = {
    val sha1 = MessageDigest.getInstance("SHA-1")
    val digest = sha1.digest(IO.readBytes(file))
    ("" /: digest)(_ + "%02x".format(_))
  }

  private def aDownloadOf(file: File) =  {
    val download = new Download()
    download.setName(file.getName)
    download.setContentType("application/java-archive")
    download.setSize(file.length())
    download.setDescription(sha1sum(file))
    download
  }

  private def readInput(msg:String) = SimpleReader.readLine(msg) getOrElse sys.error("Failed to grab input")

  private def readHidden(msg:String) = SimpleReader.readLine(msg, Some('*')) getOrElse sys.error("Failed to grab input")


  object Dependencies {
    lazy val testLibs    = Seq(
      "org.mockito"       %   "mockito-all" % "1.9.0" % "test",
      "org.scalatest"     %%  "scalatest"   % "1.7.2" % "test"
    )

    lazy val compileLibs = Seq(
      "asm"               %  "asm"          % "3.3.1",
      "asm"               %  "asm-commons"  % "3.3.1",
      "com.github.zhongl" %% "yascli"       % "0.1.0",
      "org.scala-lang"    % "scala-library" % "2.9.2"
    )
  }

  object Unmanaged {
    lazy val javaHome = sys.props("java.home").replace("/jre","")
    lazy val toolsFile = file(javaHome + "/lib/tools.jar")
    lazy val classpathSettings =
      if (sys.props("os.name").contains("Linux")) Seq(
        unmanagedClasspath in Compile +=  Attributed.blank(toolsFile),
        unmanagedClasspath in Test    <<= unmanagedClasspath in Compile
      )
      else Seq()
  }

}
