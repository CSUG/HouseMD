[中文使用指南](https://github.com/zhongl/HouseMD/wiki/UseGuideCN-0-2-0)

HouseMD is a interactive command-line tool for dianosing Java process in runtime.
It's inspiration came from [BTrace](http://kenai.com/projects/btrace), but more easier to use.

# Features

- Display loaded classes information
    - name
    - source file(.class or .jar)
    - classloaders
- Display invocation trace summary
    - method full name and sign
    - class loader of method declaring class
    - total invoked times
    - avg elapse millis
- Output invocation detail
    - timestamp
    - elapse millis
    - call thread
    - this object
    - class name
    - method name
    - arguments
    - result or exception
- Output invocation stack trace
- Auto-completion
- Awesome features you can provide by forking me

[Test cases]() would show more specification details.

# Getting started

## Pre-requirement

- JDK 6 +

## One-command install (On Linux or MacOSX)

    > curl -Lk https://raw.github.com/zhongl/HouseMD/master/bin/install | bash

- Please try normal install on Window.

## Normal install

- Click [here](https://github.com/downloads/zhongl/HouseMD/housemd-assembly-0.2.0.jar) download executable jar
- Run it as:

    > java -Xbootclasspath/a:$JAVA_HOME/lib/tools.jar -jar housemd-assembly-x.x.x.jar

> Caution: In Mac OSX, the `-Xbootclasspath` is no needed.

# How to use

First all, type:

    > housemd -h

A help infomation shows up like:

    Usage: housemd [OPTIONS] pid
    	a runtime diagnosis tool of JVM.
    Options:
    	-h, --help
    		show help infomation of this command.
    	-p, --port=[INT]
    		set console local socket server port number.
    		default: 54321
    Parameters:
    	pid
    		id of process to be diagnosing.


Suppost your target pid is `1234` (you can use `jps` or `ps` get it), and then input:

    > housemd 1234

After seen prompt `housemd>`, input `help` then you get help infomation like this:

    housemd> help

    quit      terminate the process.
    help      display this infomation.
    trace     display or output infomation of method invocaton.
    loaded    display loaded classes information.

You can also input `help loaded` and get help infomation of `loaded` as blow:

    housemd> help loaded
    Usage: loaded [OPTIONS] name
        display loaded classes information.
    Options:
        -h, --classloader-hierarchies
            display classloader hierarchies of loaded class.
    Parameters:
        name
            class name without package name.

# Trace schemas

## Summary statistics

    | method full name         | class loader                              | invoked   |  avg elapse|
    TraceTarget.addOne(int)    sun.misc.Launcher$AppClassLoader@1cde100            2           34ms


## Detail line

    | date     | time   | elapse | thread name |     this object      | method full name  | arguemnt(s) |result or exception
    2012-06-13 07:59:33 1ms      [main]        TraceTarget$B@1137d4a4 TraceTarget$B.mC    [123]         void

- The delimiter is one `white space`,
- method full name contains: package, class name and method name

Having fun!
