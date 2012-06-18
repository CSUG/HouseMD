package com.github.zhongl.housemd.command;

import com.github.zhongl.yascli.PrintOut;

import java.lang.instrument.Instrumentation;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class Inspect extends TransformCommand {
    public Inspect(Instrumentation inst, PrintOut out) {
        super("", "", inst, out);
    }

}
