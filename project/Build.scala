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

import sbt._
import sbt.Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object Build extends sbt.Build {
  import Dependencies._
  import Unmanaged._

  lazy val root = Project(
    id       = "housemd",
    base     = file("."),
    settings = Defaults.defaultSettings ++ classpathSettings ++ assemblySettings ++ Seq(
      name                := "housemd",
      organization        := "com.github.zhongl",
      version             := "0.2.3",
      scalaVersion        := "2.9.2",
      scalacOptions       ++= Seq("-unchecked", "-deprecation"),
      resolvers           += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
      libraryDependencies :=  compileLibs ++ testLibs,
      packageOptions      +=  Package.ManifestAttributes(
        ("Main-Class","com.github.zhongl.housemd.house.House"),
        ("Agent-Class","com.github.zhongl.housemd.duck.Duck"),
        ("Can-Retransform-Classes","true"),
        ("Can-Redefine-Classes","true")
      ),
      test in assembly := {},
      parallelExecution in Test := false,
      excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
        cp filter {_.data.getName == "tool.jar"}
      }
    )
  )

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
