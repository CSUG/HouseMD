import sbt._

object PluginDef extends Build {
  override def projects = Seq(root)

  lazy val root     = Project("plugins", file(".")) dependsOn (proguard)
  lazy val proguard = uri("git://github.com/senia-psm/xsbt-proguard-plugin.git")
}