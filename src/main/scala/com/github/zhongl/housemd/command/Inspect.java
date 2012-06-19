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

package com.github.zhongl.housemd.command;

import com.github.zhongl.housemd.instrument.Hook;
import com.github.zhongl.yascli.PrintOut;

import java.lang.instrument.Instrumentation;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class Inspect extends TransformCommand {

    public Inspect(Instrumentation inst, PrintOut out) {
        super("inspect", "display fields of a class.", inst, out);
    }

    @Override
    public boolean isCandidate(Class<?> klass) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDecorating(Class<?> klass, String methodName) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Hook hook() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
