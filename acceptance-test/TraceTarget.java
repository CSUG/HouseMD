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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.*;
import java.lang.Class;
import java.lang.ClassLoader;
import java.lang.ClassNotFoundException;
import java.lang.Exception;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.lang.Integer;
import java.lang.NoSuchMethodException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;
import java.lang.reflect.InvocationTargetException;

public class TraceTarget {

    public static void main(String[] args) throws Exception {
        Object o = loadClass();

        while (true) {
            addOne(0);
            o.getClass().getMethod("m", String.class).invoke(o, "123");
            try {
                Thread.sleep(500L);
            } catch (Exception e) {
                break;
            }
        }
    }

    private static Object loadClass() throws Exception {
        return Class.forName("TraceTarget$A", false, new CL()).getConstructor(String.class).newInstance("123");
    }

    public static int addOne(int i) {
        return i + 1;
    }

    public static class CL extends ClassLoader {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            Class<?> aClass = findLoadedClass(name);
            if (aClass != null) return aClass;
            if (name.startsWith("java") || name.startsWith("com.sun") || name.startsWith("sun")) {
                return getParent().loadClass(name);
            } else {
                try {
                    InputStream inputStream = getResourceAsStream(name + ".class");
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    int read = 0;
                    while ((read = inputStream.read()) > -1) outputStream.write(read);
                    byte[] bytes = outputStream.toByteArray();
                    return defineClass(name, bytes, 0, bytes.length);
                } catch (Exception e) {
                    throw new ClassNotFoundException(name, e);
                }
            }
        }
    }

    public static class A {
        static {
            System.out.println(A.class.getClassLoader());
        }

        public final String s;
        public final B b = new B();

        public A(String s) {
            this.s = s;
        }

        public void m(int i, String s) {
        }

        public final void m(String s) {
            b.m(1);
            b.m(s);
            b.m(1, 2);
        }
    }

    public final static class B extends D implements C {
        public void mC(String s) {
        }

        public void mD(int i, int j) {
        }
    }

    public interface C {
        void mC(String s);
    }

    public static abstract class D {
        public void m(int i) {
        }

        public abstract void mD(int i, int j);
    }
}