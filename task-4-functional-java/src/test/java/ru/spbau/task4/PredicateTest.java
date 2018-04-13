package ru.spbau.task4;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static ru.spbau.task4.PredicateTest.TestEnum.*;

class PredicateTest {

    @Test
    void predicate_can_be_called() {
        Predicate<String> isEmpty = String::isEmpty;

        assertThat(isEmpty.apply("")).isTrue();
        assertThat(isEmpty.apply("not empty")).isFalse();
    }

    @Test
    void predicate_is_actually_a_function() {
        Predicate<Integer> isEven = i -> i % 2 == 0;

        Function1<Integer, Boolean> isEvenFunc = isEven;

        assertThat(isEvenFunc.apply(10)).isTrue();
    }

    @Test
    void OR_works_for_predicates() {
        Predicate<String> isFoo = s -> s.equals("foo");
        Predicate<String> isBoo = s -> s.equals("boo");

        Predicate<String> fooOrBoo = isFoo.or(isBoo);
        Predicate<String> booOrFoo = isBoo.or(isFoo);

        assertThat(fooOrBoo.apply("foo")).isTrue();
        assertThat(booOrFoo.apply("foo")).isTrue();

        assertThat(fooOrBoo.apply("boo")).isTrue();
        assertThat(booOrFoo.apply("boo")).isTrue();

        assertThat(fooOrBoo.apply("other")).isFalse();
        assertThat(booOrFoo.apply("other")).isFalse();
    }

    @Test
    void AND_works_for_predicates() {
        Predicate<String> startsWithFoo = s -> s.startsWith("foo");
        Predicate<String> endsWithBoo = s -> s.endsWith("boo");

        Predicate<String> fooSomethingBoo = startsWithFoo.and(endsWithBoo);
        Predicate<String> fooSomethingBooRev = endsWithBoo.and(startsWithFoo);

        assertThat(fooSomethingBoo.apply("fooboo")).isTrue();
        assertThat(fooSomethingBooRev.apply("fooboo")).isTrue();

        assertThat(fooSomethingBoo.apply("boo")).isFalse();
        assertThat(fooSomethingBooRev.apply("boo")).isFalse();

        assertThat(fooSomethingBoo.apply("foo")).isFalse();
        assertThat(fooSomethingBooRev.apply("foo")).isFalse();

        assertThat(fooSomethingBoo.apply("other")).isFalse();
        assertThat(fooSomethingBooRev.apply("other")).isFalse();
    }

    @Test
    void OR_can_take_predicate_with_wider_type() {
        Predicate<Object> objectPredicate = Predicate.constFalse();
        Predicate<String> stringPredicate = Predicate.constTrue();

        Predicate<String> trueOrFalse = stringPredicate.or(objectPredicate);

        assertThat(trueOrFalse.apply(null)).isTrue();
    }

    @Test
    void AND_can_take_predicate_with_wider_type() {
        Predicate<Object> objectPredicate = Predicate.constFalse();
        Predicate<String> stringPredicate = Predicate.constTrue();

        Predicate<String> trueAndFalse = stringPredicate.and(objectPredicate);

        assertThat(trueAndFalse.apply(null)).isFalse();
    }

    @Test
    void negation_of_predicate_works() {
        Predicate<Integer> isPositive = i -> i > 0;

        Predicate<Integer> negativeOrZero = isPositive.not();

        assertThat(negativeOrZero.apply(10)).isFalse();
        assertThat(negativeOrZero.apply(0)).isTrue();
        assertThat(negativeOrZero.apply(-10)).isTrue();
    }

    enum TestEnum { ONE, TWO, THREE }

    @Test
    void constant_functions_return_constants() {
        Predicate<TestEnum> alwaysFalse = Predicate.constFalse();
        Predicate<TestEnum> alwaysTrue = Predicate.constTrue();

        assertThat(Arrays.asList(ONE, TWO, THREE, null)).allMatch(alwaysTrue::apply);
        assertThat(Arrays.asList(ONE, TWO, THREE, null)).noneMatch(alwaysFalse::apply);
    }
}