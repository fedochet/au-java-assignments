package ru.spbau.task4;

public interface Predicate<T> extends Function1<T, Boolean> {

    //private
    Predicate<?> ALWAYS_TRUE = arg -> true;

    //private
    Predicate<?> ALWAYS_FALSE = ALWAYS_TRUE.not();

    default Predicate<T> or(Predicate<? super T> other) {
        return arg -> apply(arg) || other.apply(arg);
    }

    default Predicate<T> and(Predicate<? super T> other) {
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
