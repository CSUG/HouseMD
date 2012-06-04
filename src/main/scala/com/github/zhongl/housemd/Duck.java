/*
 * Copyright 2012 zhongl
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

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
public class Duck {
    public static void agentmain(String arguments, Instrumentation instrumentation) throws Exception {
        String[] parts = arguments.split("\\s+", 3);
        String duckClassName = parts[0];
        int port = Integer.parseInt(parts[1]);

        ClassLoader parentClassLoader = Duck.class.getClassLoader();
        ClassLoader classLoader = new ClassLoader(parentClassLoader) {
        };

        Class<?>[] commandClasses = loadClasses(parts[2].split("\\s+"), classLoader);

        Runnable executor = (Runnable) classLoader.loadClass(duckClassName)
                .getConstructor(Instrumentation.class, int.class, Class[].class)
                .newInstance(instrumentation, port, commandClasses);

        Thread thread = new Thread(executor, "HouseMD-Duck");
        thread.setDaemon(true);
        thread.run();
    }

    private static Class<?>[] loadClasses(String[] classNames, ClassLoader classLoader) throws ClassNotFoundException {
        Class<?>[] classes = (Class<?>[]) Array.newInstance(Class.class, classNames.length);
        for (int i = 0; i < classes.length; i++) {
            classes[i] = classLoader.loadClass(classNames[i]);
        }
        return classes;
    }

}
