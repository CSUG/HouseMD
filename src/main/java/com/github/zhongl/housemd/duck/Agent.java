/*
 * Copyright 2013 zhongl
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.zhongl.housemd.duck;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.logging.Logger;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a> */
public class Agent {

    public static void agentmain(String argumentsLine, Instrumentation instrumentation) throws Exception {
        final int CLASSPATH = 0;
        final int DUCK_NAME = 1;
        final int PORT = 2;
        final Logger logger = Logger.getLogger("HouseMD");

        String[] arguments = argumentsLine.split("\\s+");
        logger.info("Loading with arguments: " + Arrays.toString(arguments));

        // Using this customed ClassLoader could remove all classes came from HouseMD after PermGen GC.
        Class<?> duck = new URLClassLoader(urls(arguments[CLASSPATH])) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                Class<?> loadedClass = findLoadedClass(name);
                if (loadedClass != null) return loadedClass;

                try {
                    Class<?> aClass = findClass(name);
                    if (resolve) resolveClass(aClass);
                    return aClass;
                } catch (Exception e) {
                    return super.loadClass(name, resolve);
                }
            }
        }.loadClass(arguments[DUCK_NAME]);

        duck.getMethod("diagnose").invoke(who(duck, arguments[PORT], instrumentation));
        logger.info("Loaded");
    }

    private static Object who(Class<?> aClass, String port, Instrumentation instrumentation) throws Exception {
        return aClass.getConstructor(String.class, Instrumentation.class).newInstance(port, instrumentation);
    }

    private static URL[] urls(String classpath) throws MalformedURLException {
        File root = new File(classpath);

        if (!root.exists() || !root.isDirectory())
            throw new IllegalArgumentException(classpath + "should be existed directory.");

        File[] jars = root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        URL[] urls = new URL[jars.length + 1];
        urls[0] = url(root);

        for (int i = 1; i < urls.length; i++) {
            urls[i] = url(jars[i - 1]);
        }

        return urls;
    }

    private static URL url(File file) throws MalformedURLException {return file.toURI().toURL();}

}
