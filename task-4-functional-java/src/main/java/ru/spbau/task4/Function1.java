package ru.spbau.task4;

import java.util.Objects;

public abstract class Function1<T, R> {
    public abstract R apply(T arg);

    public final <V> Function1<T, V> compose(Function1<? super R, ? extends V> other) {
        Objects.requireNonNull(other, "Cannot compose with null function!");

        return new Function1<T, V>() {
            @Override
            public V apply(T arg) {
                return other.apply(Function1.this.apply(arg));
            }
        };
    }
}
