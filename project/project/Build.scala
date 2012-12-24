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