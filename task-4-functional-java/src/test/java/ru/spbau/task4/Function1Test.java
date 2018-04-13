package ru.spbau.task4;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

class Function1Test {

    @Test
    void function_can_be_called() {
        Function1<Integer, Integer> square = createFunction1(i -> i * i);

        assertThat(square.apply(10)).isEqualTo(100);
    }

    @Test
    void composition_works_with_exact_types() {
        Function1<Integer, String> toString = createFunction1(i -> i.toString());
        Function1<String, Integer> length = createFunction1(String::length);

        Function1<String, String> lengthToString = length.compose(toString);

        assertThat(lengthToString.apply("hello world")).isEqualTo("11");
    }

    private <T, V> Function1<T, V> createFunction1(Function<T, V> f) {
        return new Function1<T, V>() {
            @Override
            public V apply(T arg) {
                return f.apply(arg);
            }
        };
    }
}