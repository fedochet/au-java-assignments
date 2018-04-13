package ru.spbau.task4;

public abstract class Function1<T, M> {
    public abstract M apply(T arg);

    public final <A> Function1<T, A> compose(Function1<M, A> length) {
        return new Function1<T, A>() {
            @Override
            public A apply(T arg) {
                return length.apply(Function1.this.apply(arg));
            }
        };
    }
}
