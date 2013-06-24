addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.4.0")

resolvers += Resolver.url("sbt-plugin-snapshots", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)

addSbtPlugin("org.scala-sbt" % "xsbt-proguard-plugin" % "0.1.3-SNAPSHOT")