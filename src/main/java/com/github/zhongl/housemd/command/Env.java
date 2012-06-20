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

import static com.github.zhongl.yascli.JavaConvertions.defaultConverter;
import static com.github.zhongl.yascli.JavaConvertions.get;
import static com.github.zhongl.yascli.JavaConvertions.is;
import static com.github.zhongl.yascli.JavaConvertions.list;
import static com.github.zhongl.yascli.JavaConvertions.manifest;
import static com.github.zhongl.yascli.JavaConvertions.none;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import jline.console.completer.Completer;
import scala.Function0;

import com.github.zhongl.yascli.Command;
import com.github.zhongl.yascli.PrintOut;


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class Env extends Command implements Completer {

    private final Function0 regexable = flag(list("-e", "--regex"), "enable name as regex pattern.");

    private final Function0<String> keyName = parameter("name", "system env key name.", none(String.class), manifest(String.class), defaultConverter());

    public Env(PrintOut out) {
        super("env", "display system env.", out);
    }

    @Override
    public void run() {
        if (is(regexable))
            listEnvMatchs(get(keyName));
        else
            printEnvEquals(get(keyName));
    }

    private void printEnvEquals(String key) {
        String value = System.getenv(key);
        if (value == null) println("Invalid key " + key);
        else println(key + " = " + value);
    }

    private void listEnvMatchs(String regex) {
        SortedMap<String, String> sortedMap = new TreeMap<String, String>();
        int maxKeyLength = 0;
        for (String key : System.getenv().keySet()) {
            if (!key.matches(regex)) continue;
            maxKeyLength = Math.max(maxKeyLength, key.length());
            sortedMap.put(key, System.getenv(key));
        }

        if (sortedMap.isEmpty()) return;

        String format = "%1$-" + maxKeyLength + "s = %2$s";
        for (String key : sortedMap.keySet())
            println(String.format(format, key, sortedMap.get(key)));
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        Map<String, String> env = System.getenv();
        Set<String> keys = env.keySet();

        TreeSet<String> sortedKeySet = new TreeSet<String>(keys);
        SortedSet<String> tail = sortedKeySet.tailSet(buffer);
        for (String k : tail) {
            if (k.startsWith(buffer)) candidates.add(k);
        }
        if (candidates.isEmpty()) return -1;
        return cursor - buffer.length();
    }
}
