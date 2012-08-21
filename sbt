#! /bin/bash
SBT_JAR=`dirname $0`/sbt-launch.jar

if [ ! -e $SBT_JAR ]; then
    curl http://typesafe.artifactoryonline.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.11.3-2/sbt-launch.jar -o $SBT_JAR
fi

#HTTP_PROXY="-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=8087

SBT_OPTS="$SBT_OPTS -Xms512M -Xmx1536M -Xss1M -XX:MaxPermSize=384M"
SBT_OPTS="$SBT_OPTS -XX:+CMSClassUnloadingEnabled"
SBT_OPTS="$SBT_OPTS -Dsbt.boot.directory=$HOME/.sbt"

java $HTTP_PROXY $SBT_OPTS -jar $SBT_JAR "$@"