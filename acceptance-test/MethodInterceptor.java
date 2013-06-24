import sun.reflect.Reflection;

import java.lang.ClassLoader;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Throwable;


public class MethodInterceptor {

    public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args,
                            MethodProxy proxy) throws Throwable {

        final ClassLoader loader = MethodInterceptor.class.getClassLoader();

        Global.enter("class", "method", obj, loader, args, Global.ARGS || Global.NOW)

        try {
            Object result = method.invoke(obj, args);
            Global.leave(true, result, Global.RESULT || Global.NOW || Global.STACK)
            return reuslt;
        } catch (Throwable t) {
            Global.leave(true, t, Global.RESULT || Global.NOW || Global.STACK)
            throw t;
        }

    }

}