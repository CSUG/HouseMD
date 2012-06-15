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

package com.github.zhongl.housemd;

import com.github.zhongl.yascli.Command;
import com.github.zhongl.yascli.PrintOut;
import jline.console.completer.Completer;
import scala.Function0;

import java.lang.instrument.Instrumentation;
import java.util.List;

import static com.github.zhongl.housemd.JavaConvertions.*;


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class Env extends Command implements Completer {

    private final Function0<scala.Boolean> regexable = (Function0<scala.Boolean>) flag(list("-e", "--regex"), "enable name as regex pattern");

    private final Function0<String> getKeyName = parameter("name", "system env key name.", none(String.class), manifest(String.class), defaultConverter());

    public Env(Instrumentation instrumentation, PrintOut out) {
        super("env", "display a system env key's value.", out);
    }

    @Override
    public void run() {
        String name = getKeyName.apply();
        String value = System.getenv(name);
        if (value == null) println("Invalid key " + name); else println(value);
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
