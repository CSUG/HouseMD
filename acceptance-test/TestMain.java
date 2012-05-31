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

import java.lang.*;
import java.lang.Class;
import java.lang.ClassLoader;
import java.lang.ClassNotFoundException;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.Thread;
import java.lang.reflect.Method;

public class TestMain {
    public static void main(String[] args) throws Exception{
        ClassLoader cl = new ClassLoader() {
            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("java") || name.startsWith("com.sun") | name.startsWith("sun")|| name.equals("TraceTarget"))
                    return super.loadClass(name, resolve);
                else {
                    Class c = findLoadedClass(name);
                    if (c == null) c = findClass(name);
                    if (resolve) resolveClass(c);
                    return c;
                }
            }
        };

        Class<?> aClass = cl.loadClass("TraceTarget");
        Object o = aClass.newInstance();
        Method addOne = aClass.getMethod("addOne", int.class);

        while (true) {
            addOne.invoke(o, 0);
            try {
                Thread.sleep(1000L);
            } catch (Exception e) {
                break;
            }
        }
    }


}