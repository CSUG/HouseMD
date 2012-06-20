#! /bin/bash
HOME=`dirname $0`
SBT_JAR=$HOME/sbt-launch.jar

if [ ! -e $SBT_JAR ]; then
    curl http://typesafe.artifactoryonline.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.11.3-2/sbt-launch.jar -o $SBT_JAR
fi

#HTTP_PROXY="-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=8087

java $HTTP_PROXY -Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=384M -jar $SBT_JAR "$@"