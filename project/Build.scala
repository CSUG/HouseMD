import sbt._
import sbt.Keys._

object ProjectBuild extends Build {

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "insider",
      organization := "com.github.zhongl",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.9.2",
      unmanagedClasspath in Compile += Attributed.blank(
        file("/usr/lib/jvm/java-6-sun/lib/tools.jar")
      ), 
      unmanagedClasspath in Test += Attributed.blank(
        file("/usr/lib/jvm/java-6-sun/lib/tools.jar")
      ), 
      libraryDependencies := Seq(
        "com.github.scopt" %% "scopt" % "2.0.0",
        "org.scala-lang" % "scala-library" % "2.9.2" % "runtime",
        "org.scalatest" %% "scalatest" % "1.7.2" % "test"
      )

      // add other settings here
    )
  )
}
