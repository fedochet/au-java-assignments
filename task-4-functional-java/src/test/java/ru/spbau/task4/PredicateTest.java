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

    @Test
    void OR_works_for_predicates() {
        Predicate<String> isFoo = createPredicate(s -> s.equals("foo"));
        Predicate<String> isBoo = createPredicate(s -> s.equals("boo"));

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
        Predicate<String> startsWithFoo = createPredicate(s -> s.startsWith("foo"));
        Predicate<String> endsWithBoo = createPredicate(s -> s.endsWith("boo"));

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
}