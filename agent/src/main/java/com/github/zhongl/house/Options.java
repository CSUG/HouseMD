package com.github.zhongl.house;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: <a href = "mailto:zhong.lunfu@gmail.com">zhongl</a>
 */
class Options {
    private final Map<String, String> map = new HashMap<String, String>();

    private Options(String[] options) {
        for (String option : options) {
            String[] pair = option.split("=");
            map.put(pair[0], pair[1]);
        }
    }

    public static Options parse(String arguments) {
        return new Options(arguments.split("\\s+"));
    }

    public URL[] classLoaderUrls() {
        String value = map.get("class.loader.urls");
        String[] split = value.split(":");
        Set<URL> urls = new HashSet<URL>();
        try {
            for (String path : split) {
                urls.add(new File(path).toURI().toURL());
            }
            return urls.toArray(new URL[0]);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String closureExecutorName() {
        return map.get("closure.executor.name");
    }

    public String consoleAddress() {
        return map.get("console.address");
    }
}
