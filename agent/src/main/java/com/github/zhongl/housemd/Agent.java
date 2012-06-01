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
import java.net.URLClassLoader;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
public class Agent {
    public static void agentmain(String arguments, Instrumentation instrumentation) throws Exception {
        Options options = Options.parse(arguments);

        if (options.debug()) System.out.println("agent arguments: " + arguments);
        if (options.gcAtBeginning()) System.gc();

        ClassLoader parentClassLoader = Agent.class.getClassLoader();
        URLClassLoader classLoader = URLClassLoader.newInstance(options.classLoaderUrls(), parentClassLoader);

        Runnable executor = (Runnable) classLoader.loadClass(options.mainClass())
                                                  .getConstructor(String.class, Instrumentation.class)
                                                  .newInstance(options.consoleAddress(), instrumentation);

        start(executor);
    }

    private static void start(Runnable executor) {
        executor.run();
//        Thread thread = new Thread(executor, "Closure Executor");
//        thread.setDaemon(true);
//        thread.start();
    }

}
