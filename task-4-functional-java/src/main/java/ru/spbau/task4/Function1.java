package ru.spbau.task4;

public abstract class Function1<T, R> {
    public abstract R apply(T arg);

    public final <V> Function1<T, V> compose(Function1<? super R, ? extends V> length) {
        return new Function1<T, V>() {
            @Override
            public V apply(T arg) {
                return length.apply(Function1.this.apply(arg));
            }
        };
    }
}
