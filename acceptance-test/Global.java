
import sun.reflect.Reflection;

import java.lang.*;
import java.lang.Exception;
import java.lang.Object;
import java.lang.StackTraceElement;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;
import java.lang.Throwable;
import java.lang.ref.WeakReference;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Global {
    public static final BlockingQueue<Object> QUEUE = new ArrayBlockingQueue<Object>(1000);
    public static final String                ENTER = ">";
    public static final String                LEAVE = "<";

    private static final Thread THEAD  = Thread.currentThread(); // HouseMD should load this class first.
    private static final int    NOW    = 0x01;
    private static final int    ARGS   = 0x02;
    private static final int    RESULT = 0x04;
    private static final int    STACK  = 0x08;

    /**
     * @param klass   class name
     * @param method  method name
     * @param self    this object, it would be null if method was static
     * @param loader  class loader, it would be null if class was loaded by BootClassLoader
     * @param args    method arguments
     * @param options {@link #NOW} {@link #ARGS}
     */
    public static void enter(String klass, String method, Object self, Object loader, Object[] args, int options) {
        final Thread current = Thread.currentThread();
        if (THREAD == current || QUEUE == self || Global.class == Reflection.getCallerClass(3)) return;

        final Object[] arguments = (options & ARGS) == ARGS ? args : null;
        final Long now = (options & NOW) == NOW ? System.nanoTime() : 0L;

        QUEUE.put(new Object[]{ENTER, current, klass, method, self, loader, arguments, now})
    }

    /**
     * @param normal  false leave with an exception thrown.
     * @param result  reture value or exception
     * @param options {@link #NOW} {@link #RESULT} {@link #STACK}
     */
    public static void leave(boolean normal, Object result, int options) {
        final Thread current = Thread.currentThread();
        if (THREAD == current || QUEUE == self || Global.class == Reflection.getCallerClass(3)) return;

        result = (options & RESULT) == RESULT ? result : null;
        final Long now = (options & NOW) == NOW ? System.nanoTime() : 0L;
        final StackTraceElement[] stack = (options & STACK) == STACK ? currentStackTrace() : null;

        QUEUE.put(new Object[]{LEAVE, current, normal, result, now, stack})
    }

    /**
     * <code>Global.class == Reflection.getCallerClass(3)</code> check whether it would recusive of this method or not.
     * <p/>
     * Why 3? Because
     * <code>XXX.* => Throwable.* => Global.currentStackTrace => Throwable.* => Global.enter => Reflection.getCallerClass </code>
     */
    private static StackTraceElement[] currentStackTrace() { return new Throwable().getStackTrace(); }
}