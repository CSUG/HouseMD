#! /bin/bash
HOME=`dirname $0`
SBT_JAR=$HOME/sbt-launch.jar
if [ ! -e $SBT_JAR ]; then
    curl http://typesafe.artifactoryonline.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.11.3-2/sbt-launch.jar -o $SBT_JAR
fi

java -Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=384M -jar $SBT_JAR "$@"