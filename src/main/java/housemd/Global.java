package housemd;

import sun.reflect.Reflection;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Global {
    public static final String NULL_AGENT_THREAD = "NULL_AGENT_THREAD";

    // Avoiding overflow by blocking queue
    public static final BlockingQueue<Object> QUEUE = new ArrayBlockingQueue<Object>(1000);

    // option codes
    public static final int OP_ELAPSE    = 0x01;
    public static final int OP_ARGS      = 0x02;
    public static final int OP_RESULT    = 0x04;
    public static final int OP_STACK     = 0x08;
    public static final int OP_EXCEPTION = 0x10;

    public static volatile Thread AGENT_THREAD = null;

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

        if (QUEUE == self || Global.class == Reflection.getCallerClass(3) || AGENT_THREAD == current) {
            if (AGENT_THREAD == null) QUEUE.offer(NULL_AGENT_THREAD);
            return;
        }

        QUEUE.offer(new Object[]{
                klass,
                method,
                descriptor,
                self,
                loader,
                current,
                (options & OP_EXCEPTION) == OP_EXCEPTION,
                (options & OP_ARGS) == OP_ARGS ? args : null,
                (options & OP_RESULT) == OP_RESULT ? result : null,
                (options & OP_ELAPSE) == OP_ELAPSE ? elapse : -1L,
                (options & OP_STACK) == OP_STACK ? currentStackTrace() : null
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