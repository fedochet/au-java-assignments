package ru.spbau.task4;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CollectionsTest {
    @Test
    void map_transforms_values_of_list() {
        Iterable<String> strings = Arrays.asList("a", "bc", "def");

        Iterable<Integer> lengths = Collections.map(strings, String::length);

        assertThat(lengths).containsExactly(1, 2, 3);
    }

    @Test
    void map_works_with_wider_arg_type_and_narrower_return_type() {
        Function1<Object, String> toString = Object::toString;
        Iterable<Integer> numbers = Arrays.asList(1, 2, 3);

        Iterable<Object> intStrings = Collections.<Number, Object>map(numbers, toString);

        assertThat(intStrings).containsExactly("1", "2", "3");
    }
}