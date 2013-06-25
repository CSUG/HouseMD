import java.lang.System;

public class MethodInterceptor {

    final int options = Global.RESULT | Global.ELAPSE | Global.STACK | Global.ARGS;

    public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args,
                            MethodProxy proxy) throws Throwable {

        final Object self = obj;
        final ClassLoader loader = MethodInterceptor.class.getClassLoader();

        try {
            long begin = System.nanoTime();
            Object result = method.invoke(self, args);

            Global.offset("class",
                          "method",
                          "descriptor",
                          self,
                          loader,
                          options,
                          args,
                          result,
                          System.nanoTime() - begin);

            Global.leave(self, true, result, )
            return reuslt;
        } catch (Throwable t) {

            Global.offset("class",
                          "method",
                          "descriptor",
                          self,
                          loader,
                          options | Global.EXCEPTION,
                          args,
                          t,
                          System.nanoTime() - begin);
            throw t;
        }

    }

}