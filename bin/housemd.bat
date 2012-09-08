@echo off
if 'x%JAVA_HOME%'=='x' (
	echo.
	echo Please set JAVA_HOME to JDK 6+!
	echo.
	goto end
)

:execute
rem create logs dir if it doesn't exists
if not exist logs (mkdir logs)

set TOOL_JAR=%JAVA_HOME%\lib\tools.jar
if exist %TOOL_JAR% (set BOOT_CLASSPATH=-Xbootclasspath/a:%TOOL_JAR%)
%JAVA_HOME%\bin\java -Djline.terminal=jline.UnsupportedTerminal %BOOT_CLASSPATH% -jar %~dp0housemd.jar %*

:end
rem do nothing.