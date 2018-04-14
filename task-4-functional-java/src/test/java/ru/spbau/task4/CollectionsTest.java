package ru.spbau.task4;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionsTest {
    @Test
    void map_transforms_values_of_list() {
        Iterable<String> strings = Arrays.asList("a", "bc", "def");

        Iterable<Integer> lengths = Collections.map(strings, String::length);

        assertThat(lengths).containsExactly(1, 2, 3);
    }

    @Test
    void map_works_with_wider_types() {
        Function1<Object, String> toString = Object::toString;
        Iterable<Integer> numbers = Arrays.asList(1, 2, 3);

        Iterable<Object> intStrings = Collections.<Number, Object>map(numbers, toString);

        assertThat(intStrings).containsExactly("1", "2", "3");
    }

    @Test
    void filter_removes_all_matched_elements() {
        Iterable<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);

        Iterable<Integer> even = Collections.filter(numbers, i -> i % 2 == 0);

        assertThat(even).containsExactly(2, 4, 6);
    }

    @Test
    void filter_works_with_wider_types() {
        Iterable<String> strings = Arrays.asList("a", "bc", "d");
        Predicate<Object> objectPredicate = s -> s == null;

        Iterable<CharSequence> singleChars = Collections.filter(strings, objectPredicate);

        assertThat(singleChars).isEmpty();
    }
}