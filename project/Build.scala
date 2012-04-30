import sbt._
import sbt.Keys._
import sbtassembly.Plugin._

object Build extends sbt.Build {

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ assemblySettings ++ Seq(
      name := "house",
      organization := "com.github.zhongl",
      version := "0.1.0",
      scalaVersion := "2.9.2",
      unmanagedClasspath in Compile += Attributed.blank(
        file("/usr/lib/jvm/java-6-sun/lib/tools.jar")
      ), 
      unmanagedClasspath in Test <<= unmanagedClasspath in Compile,
      unmanagedClasspath in Runtime <<= unmanagedClasspath in Compile,
      libraryDependencies := Seq(
        "asm" % "asm" % "3.3.1",
        "asm" % "asm-commons" % "3.3.1",
        "com.beust" % "jcommander" % "1.20",
        "org.scala-lang" % "scala-library" % "2.9.2",
        "org.mockito" % "mockito-all" % "1.9.0" % "test",
        "org.scalatest" %% "scalatest" % "1.7.2" % "test"
      ),
      packageOptions += Package.ManifestAttributes(
        ("Main-Class","com.github.zhongl.house.HouseMD"),
        ("Agent-Class","com.github.zhongl.house.Diagnosis"),
        ("Premain-Class","com.github.zhongl.house.Diagnosis"),
        ("Can-Retransform-Classes","true"),
        ("Can-Redefine-Classes","true")
      )
      // add other settings here
    )
  )
}
