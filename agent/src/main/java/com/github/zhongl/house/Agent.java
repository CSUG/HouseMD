package com.github.zhongl.house;

import java.lang.instrument.Instrumentation;
import java.net.URLClassLoader;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
public class Agent {
    public static void agentmain(String arguments, Instrumentation instrumentation) throws Exception {
        Options options = Options.parse(arguments);
        ClassLoader parentClassLoader = Agent.class.getClassLoader();
        URLClassLoader classLoader = URLClassLoader.newInstance(options.classLoaderUrls(), parentClassLoader);

        Runnable executor = (Runnable) classLoader.loadClass(options.closureExecutorName())
                                                  .getConstructor(String.class, Instrumentation.class)
                                                  .newInstance(options.consoleAddress(), instrumentation);

        Thread thread = new Thread(executor, "Closure Executor");
        thread.setDaemon(true);
        thread.start();
    }

}
