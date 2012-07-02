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

import com.github.zhongl.housemd.instrument.Context;
import com.github.zhongl.housemd.instrument.Hook;
import com.github.zhongl.yascli.PrintOut;
import scala.Function0;
import scala.runtime.AbstractFunction1;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.zhongl.housemd.misc.ReflectionUtils.*;
import static com.github.zhongl.yascli.JavaConvertions.manifest;
import static com.github.zhongl.yascli.JavaConvertions.none;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class Inspect extends TransformCommand implements FieldFilterCompleter {

    private final Function0<FieldFilter> fieldFilter = parameter(
            "field-filter",
            "field filter pattern like \"ClassSimpleName.fieldName\".",
            none(FieldFilter.class),
            manifest(FieldFilter.class),
            new AbstractFunction1<String, FieldFilter>() {
                @Override
                public FieldFilter apply(String value) {
                    return new FieldFilter(value);
                }
            });

    private final Instrumentation inst;

    public Inspect(Instrumentation inst, PrintOut out) {
        super("inspect", "display fields of a class.", inst, out);
        this.inst = inst;
    }

    public Instrumentation inst() {
        return inst;
    }

    @Override
    public boolean isCandidate(Class<?> klass) {
        return fieldFilter.apply().filter(klass);
    }

    @Override
    public boolean isDecorating(Class<?> klass, String methodName) {
        return true;
    }

    @Override
    public Hook hook() {
        return new Hook() {
            private final Set<Object> targets = new HashSet<Object>();
            private final FieldFilter accessor = fieldFilter.apply();

            public void enterWith(Context context) {
            }

            public void exitWith(Context context) {
                targets.add(context.thisObject());
            }

            public void heartbeat(long now) {
                if (targets.isEmpty())
                    println("Can't inspect " + accessor + " because there's no invocation on " + accessor.classSimpleName);
                else
                    for (Object target : targets) printStat(target);
                println();
            }

            private void printStat(Object target) {
                try {
                    println(accessor + " " + accessor.getField(target) + " " + target + " " + target.getClass().getClassLoader());
                } catch (Exception e) {
                    error(e);
                }
            }

            public void finalize(scala.Option<Throwable> throwable) {
                heartbeat(0L); // last print
                targets.clear();
            }
        };
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        return ClassSimpleNameCompleter$class.complete(this, buffer, cursor, candidates);
    }

    @Override
    public int completeClassSimpleName(String buffer, int cursor, List<CharSequence> candidates) {
        return ClassMemberCompleter$class.completeClassSimpleName(this, buffer, cursor, candidates);
    }

    @Override
    public String[] collectLoadedClassNames(String prefix) {
        return ClassMemberCompleter$class.collectLoadedClassNames(this, prefix);
    }

    @Override
    public int completeAll(String classSimpleName, int cursor, List<CharSequence> candidates) {
        return FieldFilterCompleter$class.completeAll(this, classSimpleName, cursor, candidates);
    }

    @Override
    public int complete(String simpleName, String prefix, int cursor, List<CharSequence> candidates) {
        return FieldFilterCompleter$class.complete(this, simpleName, prefix, cursor, candidates);
    }

    public int com$github$zhongl$housemd$command$ClassMemberCompleter$$super$completeClassSimpleName(
            String buffer, int cursor, List<CharSequence> candidates) {
        return ClassSimpleNameCompleter$class.completeClassSimpleName(this, buffer, cursor, candidates);
    }

    static class FieldFilter {

        private final String classSimpleName;
        private final String fieldName;

        public FieldFilter(String value) {
            String[] split = value.split("\\.");
            if (split.length != 2)
                throw new IllegalArgumentException(", it should be \"ClassSimpleName.fieldName\"");
            classSimpleName = split[0];
            fieldName = split[1];
        }

        private boolean filterOnly(String className, String superClassName, String[] interfaceNames) {
            if (classSimpleName.endsWith("+")) {
                String realname = classSimpleName.substring(0, classSimpleName.length() - 1);
                return (simpleNameOf(className).equals(realname) ||
                        superClassName != null && simpleNameOf(superClassName).equals(realname)) ||
                        contain(realname, interfaceNames);
            } else {
                return simpleNameOf(className).equals(classSimpleName);
            }
        }

        private boolean contain(String realname, String[] interfaceNames) {
            for (String interfaceName : interfaceNames) {
                if (simpleNameOf(interfaceName).equals(realname)) return true;
            }
            return false;
        }

        public boolean filter(Class<?> c) {
            String superClassName = (c.getSuperclass() == null) ? null : c.getSuperclass().getName();
            Class<?>[] interfaces = c.getInterfaces();
            String[] interfaceNames = new String[interfaces.length];
            for (int i = 0; i < interfaceNames.length; i++) {
                interfaceNames[i] = interfaces[i].getName();
            }
            return filterOnly(c.getName(), superClassName, interfaceNames);
        }

        public Object getField(Object obj) throws Exception {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        }

        @Override
        public String toString() {
            return classSimpleName + "." + fieldName;
        }
    }
}
