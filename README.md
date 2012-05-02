[中文使用指南](https://github.com/zhongl/HouseMD/wiki/UseGuideCN)

HouseMD is a command-line tool for dianosing Java process in runtime.
It's inspiration came from [BTrace](http://rdc.taobao.com/team/jm/archives/509), but more easier to use.

# Features

- output process summary
- output enviroment and properties
- output loaded classes and their source
- output invocation trace

# Diagnosis report example

    #Diagnosis report
    > created at Mon Apr 30 16:37:23 CST 2012

    ##Summary

            name = 2097@local
            arguments = []
            starTime = Mon Apr 30 16:37:18 CST 2012
            upTime = 0 hours 0 minutes 0 seconds

    ##Enviroment

            SHELL = /bin/bash
            GNOME_KEYRING_PID = 1276
            ...

    ##Properties

            java.io.tmpdir = /tmp
            line.separator =

            path.separator = :
            sun.management.compiler = HotSpot Tiered Compilers
            ...

    ##Loaded classes: .*TraceTarget.*

            TraceTarget -> file:/path/to/TraceTarget.class

    ##Traces: .*TraceTarget.*

            2012-04-30 16:37:28 0ms main TraceTarget.addOne 0 1
            2012-04-30 16:37:28 0ms main TraceTarget.addOne 0 1
            2012-04-30 16:37:29 0ms main TraceTarget.addOne 0 1
            2012-04-30 16:37:29 0ms main TraceTarget.addOne 0 1
            2012-04-30 16:37:30 0ms main TraceTarget.addOne 0 1
            2012-04-30 16:37:30 0ms main TraceTarget.addOne 0 1

    Diagnosing ended by Timeout(3)


# Getting started

## Pre-requirement

- Java 6 +

## One-command install (On Linux or MacOSX)

    > curl https://raw.github.com/zhongl/HouseMD/master/bin/install | bash

    > house

- Please try normal install on Window.

## Normal install

- Click [here](https://github.com/downloads/zhongl/HouseMD/house-assembly-0.1.0.jar) download executable jar
- Run it as:

    > java -jar house-assembly-0.1.0.jar

# How to use

Suppost your target pid is `1234`, and trace target method regex pattern is `.*TraceTarget.*`, and then input:

    house 1234 .*TraceTraget.*

By default, you can get `diagnosis.report` file from path that your target process started up after one minute.

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

, or you can get some help from [acceptance test](https://github.com/zhongl/HouseMD/blob/master/acceptance-test/test).

# Trace line schema

    | date     | time   | elapse | thread name | method full name  | arguemnt(s) |result or exception
    2012-04-30 16:37:28 0ms      main          TraceTarget.addOne  0             1

- The delimiter is one `white space`,
- method full name contains: package, class name and method name


Have fun!