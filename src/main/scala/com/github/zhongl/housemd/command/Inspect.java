package com.github.zhongl.housemd.command;

import com.github.zhongl.housemd.instrument.Filter;
import com.github.zhongl.housemd.instrument.Hook;
import com.github.zhongl.housemd.instrument.Seconds;
import com.github.zhongl.yascli.PrintOut;

import java.lang.instrument.Instrumentation;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class Inspect extends TransformCommand {
    public Inspect(Instrumentation inst, PrintOut out) {
        super("", "", inst, out);
    }

    @Override
    public Hook hook() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Seconds timeout() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int overLimit() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Filter filter() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
