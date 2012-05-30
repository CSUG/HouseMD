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

package com.github.zhongl.house;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
class Options {
    private final Map<String, String> map;

    private Options(Map<String, String> map) {
        this.map = map;
    }

    public static Options parse(String arguments) {
        final Map<String, String> map = new HashMap<String, String>();
        for (String option : arguments.split("\\s+")) {
            String[] pair = option.split("=");
            map.put(pair[0], pair[1]);
        }
        return new Options(map);
    }

    /**
     * @return urls for URLClassLoader to find classes.
     * @see java.net.URLClassLoader
     */
    public URL[] classLoaderUrls() {
        String value = map.get("class.loader.urls");
        String[] split = value.split(":");
        Set<URL> urls = new HashSet<URL>();
        try {
            for (String path : split) {
                urls.add(new File(path).toURI().toURL());
            }
            return urls.toArray(new URL[urls.size()]);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return closure executor class full name.
     * @see Class#getName()
     */
    public String mainClass() {
        return map.get("closure.executor.name");
    }

    /**
     * @return address string, eg: "localhost:54321"
     */
    public String consoleAddress() {
        return map.get("console.address");
    }

    /**
     * @return true if you want gc all useless classes loaded by last.
     * @see System#gc()
     */
    public boolean gcAtBeginning() {
        // having value means turn on this option
        return map.containsKey("gc.at.beginning");
    }

    public boolean debug() {
        return map.containsKey("debug");
    }
}
