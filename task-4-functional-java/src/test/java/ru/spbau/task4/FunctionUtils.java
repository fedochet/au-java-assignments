package ru.spbau.task4;

import java.util.function.BiFunction;
import java.util.function.Function;

final class FunctionUtils {
    static <T, V> Function1<T, V> createFunction1(Function<T, V> f) {
        return new Function1<T, V>() {
            @Override
            public V apply(T arg) {
                return f.apply(arg);
            }
        };
    }

    static <T, U, V> Function2<T, U, V> createFunction2(BiFunction<T, U, V> f) {
        return new Function2<T, U, V>() {
            @Override
            public V apply(T arg1, U arg2) {
                return f.apply(arg1, arg2);
            }
        };
    }

    static <T> Predicate<T> createPredicate(java.util.function.Predicate<T> p) {
        return new Predicate<T>() {
            @Override
            public Boolean apply(T arg) {
                return p.test(arg);
            }
        };
    }
}
