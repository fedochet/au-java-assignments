package ru.spbau.task4;

import java.util.Objects;

public interface Function1<T, R> {
    R apply(T arg);

    default <V> Function1<T, V> compose(Function1<? super R, ? extends V> other) {
        Objects.requireNonNull(other, "Cannot compose with null function!");

        return arg -> other.apply(apply(arg));
    }
}
