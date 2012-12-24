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

object Build extends sbt.Build {

  override def projects = Seq(root)

  lazy val proguard = uri("git://github.com/senia-psm/xsbt-proguard-plugin.git")
  lazy val root     = Project(
    id = "plugins",
    base = file("."),
    settings = Defaults.defaultSettings ++ Seq(
      addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0")
    )
  ) dependsOn (proguard)


}