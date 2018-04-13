package ru.spbau.task4;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static ru.spbau.task4.FunctionUtils.*;

class PredicateTest {

    @Test
    void predicate_can_be_called() {
        Predicate<String> isEmpty = createPredicate(String::isEmpty);

        assertThat(isEmpty.apply("")).isTrue();
        assertThat(isEmpty.apply("not empty")).isFalse();
    }

    @Test
    void predicate_is_actually_a_function() {
        Predicate<Integer> isEven = createPredicate(i -> i % 2 == 0);

        Function1<Integer, Boolean> isEvenFunc = isEven;

        assertThat(isEvenFunc.apply(10)).isTrue();
    }
}