[中文使用指南](https://github.com/zhongl/HouseMD/wiki/UseGuideCN)

HouseMD is a interactive command-line tool for dianosing Java process in runtime.
It's inspiration came from [BTrace](http://kenai.com/projects/btrace), but more easier to use.

# Features

- Display loaded classes information
    - name
    - source file(.class or .jar)
    - classloaders
- Display invocation trace statistics
    - total times
    - failure times
    - min elapse millis
    - max elapse millis
    - avg elapse millis
- Output invocation detail
    - timestamp
    - elapse millis
    - call thread
    - class name
    - method name
    - arguments
    - result or exception
- Output invocation stack trace
- Awesome features you can provide by forking me

# Getting started

## Pre-requirement

- JDK 6 +

## One-command install (On Linux or MacOSX)

    > curl https://raw.github.com/zhongl/HouseMD/master/bin/install | bash

    > housemd

- Please try normal install on Window.

## Normal install

- Click [here](https://github.com/downloads/zhongl/HouseMD/housemd-assembly-0.2.0.jar) download executable jar
- Run it as:

    > java -Xbootclasspath/a:$JAVA_HOME/lib/tools.jar -jar housemd-assembly-x.x.x.jar

# How to use

First all, type

Suppost your target pid is `1234` (you can use `jps` or `ps` get it), and then input:

    housemd 1234

After seen prompt `housemd>`, input `help` then you get help infomation like this:

    housemd> help

    quit      terminate the process.
    help      display this infomation.
    trace     display or output infomation of method invocaton.
    loaded    display loaded classes information.

You can also input `help loaded` and get help infomation of `loaded`, as:

    housemd> help loaded
    Usage: loaded [OPTIONS] keyword
        display loaded classes information.
    Options:
        -h, --classloader-hierarchies
            display classloader hierarchies of loaded class.
    Parameters:
        keyword
            keyword which class name contains.

Your can make it faster or slower by `-t <seconds>`, eg:

    house -t 3 1234 .*TraceTraget.*

, or let `diagnosis.report` created wherever you want:

    house -o /path/to/diagnosis.report 1234 .*TraceTraget.*

# More usage

Execute command without any argument, you will see:

    Usage: house [options] <pid> <method regex> [more method regex...]
      Options:
        -l, --loaded            regex pattern for loaded class filter
                                Default: .+
        -c, --max-probe-count   max probe count for diagnosing last
                                Default: 1000
        -o, --output            output file pattern for diagnosis report
                                Default: /path/to/diagnosis.report
        -t, --timeout           seconds for diagnosing last
                                Default: 60



# Trace line schema

    | date     | time   | elapse | thread name | method full name  | arguemnt(s) |result or exception
    2012-04-30 16:37:28 0ms      main          TraceTarget.addOne  0             1

- The delimiter is one `white space`,
- method full name contains: package, class name and method name


Have fun!