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

import scala.*;
import scala.collection.immutable.List;
import scala.reflect.Manifest;
import scala.reflect.Manifest$;
import scala.runtime.AbstractFunction1;

import java.lang.Boolean;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class JavaConvertions {
    private static final AbstractFunction1<String, String> TO_STRING = new AbstractFunction1<String, String>() {
        @Override
        public String apply(String value) {
            return value;
        }
    };

    public static <T> Manifest<T> manifest(Class<T> klass) {
        return Manifest$.MODULE$.classType(klass);
    }

    public static <T> Option<T> none(Class<T> klass) {
        return Option$.MODULE$.empty();
    }

    public static <T> Option<T> some(T value) {
        return Some$.MODULE$.apply(value);
    }

    public static Function1<String, String> defaultConverter() {
        return TO_STRING;
    }

    public static <T> List<T> list(T... values) {
        return List.fromArray(values);
    }

    public static <T> T get(Function0<T> fun) {
        return fun.apply();
    }

    public static boolean is(Function0 fun) {
        return (Boolean) get(fun);
    }

    private JavaConvertions() {}
}
