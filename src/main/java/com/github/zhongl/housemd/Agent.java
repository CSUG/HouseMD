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

package com.github.zhongl.housemd;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a> */
public class Agent {

    public static void agentmain(String argsLine, Instrumentation inst) throws Exception {
        String[] arguments = argsLine.split("\\s+");
        String classpath = arguments[0];
        String port = arguments[1];

        System.out.println("HouseMD - Loading with arguments: " + Arrays.toString(arguments));

        PermGenGCFriendlyClassLoader loader = new PermGenGCFriendlyClassLoader(urls(classpath), inst);
        Class<?> c = loader.loadClass("com.github.zhongl.housemd.Cameron");

        Object cameron = c.getConstructor(String.class, Instrumentation.class, Runnable.class)
                .newInstance(port, inst, loader.cleanTask);

        c.getMethod("diagnose").invoke(cameron);

        System.out.println("HouseMD - Loaded");
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

class PermGenGCFriendlyClassLoader extends URLClassLoader {

    final Runnable cleanTask;

    PermGenGCFriendlyClassLoader(URL[] urls, final Instrumentation inst) {
        super(urls);
        cleanTask = new Runnable() {
            @Override
            public void run() {
                // break strong reference from ThreadLocal avoid PermGen leak
                for (Class<?> c : inst.getAllLoadedClasses()) {
                    if (notContains(c)) continue;
                    Field[] fields = c.getDeclaredFields();
                    for (Field f : fields) {
                        if (!isThreadLocal(f)) continue;
                        f.setAccessible(true);
                        try {
                            ((ThreadLocal) f.get(null)).remove();
                        } catch (Exception ignore) {}
                    }
                }
            }
        };
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) return loadedClass;

        try {
            // Load HouseMD(or dependencies)'s classes here first.
            Class<?> aClass = findClass(name);
            if (resolve) resolveClass(aClass);
            return aClass;
        } catch (Exception e) {
            return super.loadClass(name, resolve);
        }
    }

    private boolean notContains(Class<?> c) {return c.getClassLoader() != this;}

    private boolean isThreadLocal(Field f) {
        return ThreadLocal.class.isAssignableFrom(f.getType()) && Modifier.isStatic(f.getModifiers());
    }
}
