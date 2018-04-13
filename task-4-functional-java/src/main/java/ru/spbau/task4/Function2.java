package ru.spbau.task4;

public abstract class Function2<T1, T2, M> {
    public abstract M apply(T1 arg1, T2 arg2);
    public <A> Function2<T1, T2, A> compose(Function1<M, A> f) {
        return new Function2<T1, T2, A>() {
            @Override
            public A apply(T1 arg1, T2 arg2) {
                return f.apply(Function2.this.apply(arg1, arg2));
            }
        };
    }
}
