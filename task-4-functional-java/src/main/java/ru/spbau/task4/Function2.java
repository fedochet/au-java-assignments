package ru.spbau.task4;

import java.util.Objects;

public interface Function2<T1, T2, M> {
    M apply(T1 arg1, T2 arg2);

    default <A> Function2<T1, T2, A> compose(Function1<? super M, ? extends A> other) {
        Objects.requireNonNull(other, "Cannot compose with null function!");

        return (arg1, arg2) -> other.apply(apply(arg1, arg2));
    }

    default Function1<T2, M> bind1(T1 arg1) {
        return arg2 -> apply(arg1, arg2);
    }

    default Function1<T1, M> bind2(T2 arg2) {
        return arg1 -> apply(arg1, arg2);
    }

    default Function1<T1, Function1<T2, M>> curry() {
        return this::bind1;
    }
}
