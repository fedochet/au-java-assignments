package ru.spbau.task4;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
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
    void result_of_map_is_a_copy() {
        List<Integer> ints = asArrayList(1, 2, 3);
        Iterable<Integer> negative = Collections.map(ints, Math::negateExact);

        ints.add(4);

        assertThat(negative).containsExactly(-1, -2, -3);
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
        Predicate<Object> objectPredicate = Objects::isNull;

        Iterable<CharSequence> singleChars = Collections.filter(strings, objectPredicate);

        assertThat(singleChars).isEmpty();
    }

    @Test
    void result_of_filter_is_a_copy() {
        List<Integer> ints = asArrayList(1, -1, 2, -2);
        Iterable<Integer> positives = Collections.filter(ints, i -> i > 0);

        ints.add(3);

        assertThat(positives).containsExactly(1, 2);
    }

    @Test
    void take_while_saves_only_matched_elements() {
        Iterable<Integer> ints = Arrays.asList(1, 2, 3, 4, 5, 3, 1, 7);

        Iterable<Integer> lessThanFive = Collections.takeWhile(ints, i -> i < 5);

        assertThat(lessThanFive).containsExactly(1, 2, 3, 4);
    }

    @Test
    void takeUnless_saves_only_first_not_matched_elements() {
        Iterable<Integer> ints = Arrays.asList(1, 2, 3, 4, 5, 3, 1, 7);

        Iterable<Integer> lessThanFive = Collections.takeUnless(ints, i -> i == 5);

        assertThat(lessThanFive).containsExactly(1, 2, 3, 4);

    }

    /**
     * Checks that foldl on array [2, 3, 4] with zero element is performed like this:
     * <p>
     * (((1 - 2) - 3) - 4
     */
    @Test
    void foldl_works_from_left_to_right() {
        Iterable<Integer> numbers = Arrays.asList(2, 3, 4);

        Integer folded = Collections.foldl(numbers, (a, b) -> a - b, 1);

        assertThat(folded).isEqualTo(-8);
    }

    @Test
    void foldl_on_empty_list_returns_zero_element() {
        Iterable<Integer> emptyList = Arrays.asList();

        String folded = Collections.foldl(emptyList, (i, j) -> "string", "zero");

        assertThat(folded).isEqualTo("zero");
    }

    @Test
    void foldl_works_with_wider_types() {
        Iterable<Integer> ints = Arrays.asList(1, 2, 3);
        Function2<Object, Object, String> secondToString = (i, j) -> "" + i + j;

        CharSequence folded = Collections.<Number, CharSequence>foldl(ints, secondToString, "str");

        assertThat(folded).isEqualTo("str123");
    }

    /**
     * Checks that foldr on array [2, 3, 4] with zero element 1 is performed like this:
     * <p>
     * 2 - (3 - (4 - 1))
     */
    @Test
    void foldr_works_from_right_to_left() {
        Iterable<Integer> numbers = Arrays.asList(2, 3, 4);

        Integer folded = Collections.foldr(numbers, (a, b) -> a - b, 1);

        assertThat(folded).isEqualTo(2);
    }

    @Test
    void foldr_on_empty_list_returns_zero_element() {
        List<Integer> emptyList = Arrays.asList();

        Integer folded = Collections.foldr(emptyList, (a, b) -> a + b, 1);

        assertThat(folded).isEqualTo(1);
    }

    @Test
    void foldr_works_with_wider_types() {
        Iterable<Integer> ints = Arrays.asList(1, 2, 3);
        Function2<Object, Object, String> secondToString = (i, j) -> "" + i + j;

        CharSequence folded = Collections.<Number, CharSequence>foldr(ints, secondToString, "str");

        assertThat(folded).isEqualTo("123str");
    }

    @SafeVarargs
    private final <T> ArrayList<T> asArrayList(T... integers) {
        return Stream.of(integers).collect(toCollection(ArrayList::new));
    }
}