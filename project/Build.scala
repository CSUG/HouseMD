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

object Build extends sbt.Build {
  import BaseSettings._
  import Dependencies._

  lazy val root = Project(id = "HouseMD", base = file("."), settings = baseSettings) aggregate(console, agent)

  lazy val console = Project(
    id = "Console",
    base = file("console"),
    settings = baseSettings ++ assemblySettings ++ Seq(
      unmanagedClasspath in Compile +=  Attributed.blank(
        file("/usr/lib/jvm/java-6-sun/lib/tools.jar")
      ),
      unmanagedClasspath in Test    <<= unmanagedClasspath in Compile,
      unmanagedClasspath in Runtime <<= unmanagedClasspath in Compile,
      libraryDependencies           :=  consoleDependencies ++ test,
      packageOptions                +=  Package.ManifestAttributes(
        ("Main-Class","com.github.zhongl.house.HouseMD")
      )
    )
  )

  lazy val agent = Project(
    id = "Agent",
    base = file("agent"),
    settings = baseSettings ++ Seq(
      libraryDependencies :=  test,
      artifactName        :=  { (scalaVersion, moduleID, artifact) => moduleID.name + "-" + moduleID.revision + ".jar" },
      javacOptions        ++= Seq("-source", "1.6", "-target", "1.6"),
      packageOptions      +=  Package.ManifestAttributes(
        ("Agent-Class","com.github.zhongl.house.Agent"),
        ("Can-Retransform-Classes","true"),
        ("Can-Redefine-Classes","true")
      )
    )
  )

  object BaseSettings {
    val baseSettings = Defaults.defaultSettings ++ Seq(
      organization  := "com.github.zhongl",
      version       := "0.2.0",
      scalaVersion  := "2.9.2"
    )
  }

  object Dependencies {
    val test = Seq(
      "org.mockito"     %   "mockito-all"   % "1.9.0" % "test",
      "org.scalatest"   %%  "scalatest"     % "1.7.2" % "test"
    )

    val consoleDependencies =  Seq(
      "asm"             % "asm"             % "3.3.1",
      "asm"             % "asm-commons"     % "3.3.1",
      "com.beust"       % "jcommander"      % "1.20",
      "org.scala-lang"  % "scala-library"   % "2.9.2" % "runtime"
    )
  }

}
