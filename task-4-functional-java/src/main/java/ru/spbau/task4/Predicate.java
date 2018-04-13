package ru.spbau.task4;

import java.util.Objects;

public interface Predicate<T> extends Function1<T, Boolean> {

    //private
    Predicate<?> ALWAYS_TRUE = arg -> true;

    //private
    Predicate<?> ALWAYS_FALSE = ALWAYS_TRUE.not();

    default Predicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other, "OR does not accept null predicates!");

        return arg -> apply(arg) || other.apply(arg);
    }

    default Predicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other, "AND does not accept null predicates!");

        return arg -> apply(arg) && other.apply(arg);
    }

    default Predicate<T> not() {
        return arg -> !apply(arg);
    }

    static <T> Predicate<T> constTrue() {
        @SuppressWarnings("unchecked")
        Predicate<T> alwaysTrue = (Predicate<T>) ALWAYS_TRUE;
        return alwaysTrue;
    }

    static <T> Predicate<T> constFalse() {
        @SuppressWarnings("unchecked")
        Predicate<T> alwaysFalse = (Predicate<T>) ALWAYS_FALSE;
        return alwaysFalse;
    }
}
