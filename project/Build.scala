/*
 * Copyright 2013 zhongl
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

import sbt._
import sbt.Keys._
import ProguardPlugin._

object Build extends sbt.Build {

  import Dependencies._
  import Unmanaged._

  val VERSION  = "0.3.0"
  val javaHome = sys.props("java.home").replace("/jre", "")

  lazy val proguard = proguardSettings ++ Seq(
    proguardOptions := Seq(
      "-keepclasseswithmembers public class * { public static void main(java.lang.String[]); }",
      "-keep class * implements org.xml.sax.EntityResolver",
      "-keep class com.github.zhongl.housemd.** { *;} ",
      "-keep class com.github.zhongl.yascli.** { *;} ",
      "-keep class scala.** { *;} ",
      "-keep class akka.** { *;} ",
      "-keepclassmembers class * { ** MODULE$;}"
    ),
    proguardLibraryJars := {
      (jdkJarPath: PathFinder).get
    }
  )

  private def jdkJarPath = {
    val home = new java.io.File(sys.props("java.home"))
    val rtJar = home / "lib" / "rt.jar"
    val toolsJar = home.getParentFile / "lib" / "tools.jar"
    val classesJar = home.getParentFile / "Classes" / "classes.jar"
    if (classesJar.asFile.exists()) // it means current os is Mac OSX
      Seq(classesJar.asFile)
    else if (rtJar.asFile.exists())
      Seq(rtJar.asFile, toolsJar.asFile)
    else
      throw new IllegalStateException("Unknown location for rt.jar")
  }

  lazy val root = Project(
    id = "housemd",
    base = file("."),
    settings = Defaults.defaultSettings ++ classpathSettings ++ proguard ++ Seq(
      name := "housemd",
      organization := "com.github.zhongl",
      version := VERSION,
      scalaVersion := "2.10.0-RC5",
      javacOptions ++= Seq("-source", "6", "-target", "6"),
      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:_"),
      resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
      libraryDependencies := compileLibs ++ testLibs,
      packageOptions += Package.ManifestAttributes(
        ("Main-Class", "com.github.zhongl.housemd.house.House"),
        ("Agent-Class", "com.github.zhongl.housemd.duck.Agent"),
        ("Can-Retransform-Classes", "true"),
        ("Can-Redefine-Classes", "true"),
        ("Signature-Version", VERSION)
      ),
      parallelExecution in Test := false
    )
  )

  object Dependencies {
    lazy val testLibs = Seq(
      "org.mockito" % "mockito-all" % "1.9.0" % "test",
      "org.scalatest" % "scalatest_2.10.0-RC5" % "1.8-B1" % "test"
    )

    lazy val compileLibs = Seq(
      "org.ow2.asm" % "asm-commons" % "4.0",
      "com.github.zhongl" % "yascli_2.10" % "0.2.0",
      "com.typesafe.akka" %% "akka-actor" % "2.1.0-RC5" cross CrossVersion.full,
      "org.scala-lang" % "scala-library" % "2.10.0-RC5"
    )
  }

  object Unmanaged {
    lazy val toolsFile         = file(javaHome + "/lib/tools.jar")
    lazy val classpathSettings =
      if (toolsFile.exists()) Seq(
        unmanagedClasspath in Compile += Attributed.blank(toolsFile),
        unmanagedClasspath in Test <<= unmanagedClasspath in Compile
      )
      else Seq()
  }

}
