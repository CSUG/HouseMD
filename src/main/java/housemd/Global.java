package housemd;

import sun.reflect.Reflection;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Global {
    // Avoiding overflow by blocking queue
    public static final BlockingQueue<Object> QUEUE = new ArrayBlockingQueue<Object>(1000);

    // HouseMD agent should load this class first
    private static final Thread AGENT_THREAD = Thread.currentThread();

    // options enums
    private static final int ELAPSE    = 0x01;
    private static final int ARGS      = 0x02;
    private static final int RESULT    = 0x04;
    private static final int STACK     = 0x08;
    private static final int EXCEPTION = 0x10;
    private static final int THREAD    = 0x20;


    public static void offer(String klass,
                             String method,
                             String descriptor,
                             Object self,
                             ClassLoader loader,
                             int options,
                             Object[] args,
                             Object result,
                             long elapse) {
        final Thread current = Thread.currentThread();
        if (AGENT_THREAD == current || QUEUE == self || Global.class == Reflection.getCallerClass(3)) return;

        QUEUE.offer(new Object[]{
                klass,
                method,
                descriptor,
                self,
                loader,
                (options & EXCEPTION) == EXCEPTION,
                (options & ARGS) == ARGS ? args : null,
                (options & RESULT) == RESULT ? result : null,
                (options & ELAPSE) == ELAPSE ? elapse : -1L,
                (options & THREAD) == THREAD ? current : null,
                (options & STACK) == STACK ? currentStackTrace() : null
        });
    }

    /**
     * <code>Global.class == Reflection.getCallerClass(3)</code> check whether it would recusive of this method or not.
     * <p/>
     * Why 3? Because
     * <code>XXX.* => Throwable.* => Global.currentStackTrace => Throwable.* => Global.enter => Reflection.getCallerClass </code>
     */
    private static StackTraceElement[] currentStackTrace() { return new Throwable().getStackTrace(); }
}