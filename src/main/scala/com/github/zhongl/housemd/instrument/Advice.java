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

package com.github.zhongl.housemd.instrument;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * See <a href="https://github.com/zhongl/HouseMD/issues/17">#17</a> for more information.
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public abstract class Advice {

    public static final String CLASS = "class";
    public static final String METHOD = "method";
    public static final String VOID_RETURN = "voidReturn";
    public static final String THIS = "this";
    public static final String ARGUMENTS = "arguments";
    public static final String DESCRIPTOR = "descriptor";
    public static final String STACK = "stack";
    public static final String STARTED = "started";
    public static final String STOPPED = "stopped";
    public static final String RESULT = "result";
    public static final String THREAD = "thread";
    public static final String SET_DELEGATE = "setDelegate";
    public static final String SET_DEFAULT_DELEGATE = "setDefaultDelegate";
    public static final String CLASS_LOADER = "classLoader";

    public static final Method ON_METHOD_BEGIN;
    public static final Method ON_METHOD_END;

    private static final AtomicReference<Object> delegate;
    private static final Map<Thread, Stack<Map<String, Object>>> threadBoundContexts;
    private static final Advice nullAdvice;
    private static final ClassLoader loader;

    static {
        try {
            ON_METHOD_BEGIN = Advice.class.getMethod("onMethodBegin", String.class, String.class, String.class, Object.class, Object[].class);
            ON_METHOD_END = Advice.class.getMethod("onMethodEnd", Object.class);
            loader = Advice.class.getClassLoader();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        nullAdvice = null;

        delegate = new AtomicReference<Object>(nullAdvice);
        threadBoundContexts = new ConcurrentHashMap<Thread, Stack<Map<String, Object>>>();
    }

    public static void setDelegate(Object obj) {
        delegate.set(obj);
    }

    public static void setDefaultDelegate() {
        delegate.set(nullAdvice);
    }

    public static void onMethodBegin(String className, String methodName, String descriptor, Object thisObject, Object[] arguments) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(CLASS, className);
        context.put(METHOD, methodName);
        context.put(CLASS_LOADER, loader);
        context.put(VOID_RETURN, isVoidReturn(descriptor));
        context.put(THIS, thisObject);
        context.put(ARGUMENTS, arguments);
        context.put(DESCRIPTOR, descriptor);
        context.put(STACK, currentStackTrace());
        context.put(STARTED, System.currentTimeMillis());
        context.put(THREAD, Thread.currentThread());
        invoke("enterWith", context);
        stackPush(context);
    }

    private static void invoke(String name, Map<String, Object> context) {
        try {
            Object o = delegate.get();
            if (o == null) return;
            o.getClass().getMethod(name, Map.class).invoke(o, context);
        } catch (Throwable ignore) {
        }
    }

    public static void onMethodEnd(Object resultOrException) {
        Map<String, Object> context = stackPop();
        context.put(STOPPED, System.currentTimeMillis());
        context.put(RESULT, resultOrException);
        invoke("exitWith", context);
    }

    private static void stackPush(Map<String, Object> context) {
        Thread t = Thread.currentThread();
        Stack<Map<String, Object>> s = threadBoundContexts.get(t);
        if (s == null) {
            s = new Stack<Map<String, Object>>();
            threadBoundContexts.put(t, s);
        }
        s.push(context);
    }

    private static Map<String, Object> stackPop() {
        return threadBoundContexts.get(Thread.currentThread()).pop();
    }

    private static StackTraceElement[] currentStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return Arrays.copyOfRange(stackTrace, 4, stackTrace.length); // trim useless stack trace elements.
    }


    private static Boolean isVoidReturn(String descriptor) {
        return descriptor.charAt(descriptor.indexOf(')') + 1) == 'V';
    }

    public abstract void enterWith(Map<String, Object> context);

    public abstract void exitWith(Map<String, Object> context);
}
