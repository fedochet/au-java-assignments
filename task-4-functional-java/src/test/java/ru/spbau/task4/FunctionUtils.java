package ru.spbau.task4;

import java.util.function.BiFunction;
import java.util.function.Function;

final class FunctionUtils {
    static <T, V> Function1<T, V> createFunction1(Function<T, V> f) {
        return f::apply;
    }

    static <T, U, V> Function2<T, U, V> createFunction2(BiFunction<T, U, V> f) {
        return f::apply;
    }
}
