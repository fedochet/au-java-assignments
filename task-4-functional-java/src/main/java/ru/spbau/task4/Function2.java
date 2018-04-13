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

    public Function1<T2, M> bind1(T1 arg1) {
        return new Function1<T2, M>() {
            @Override
            public M apply(T2 arg2) {
                return Function2.this.apply(arg1, arg2);
            }
        };
    }

    public Function1<T1, M> bind2(T2 arg2) {
        return new Function1<T1, M>() {
            @Override
            public M apply(T1 arg1) {
                return Function2.this.apply(arg1, arg2);
            }
        };
    }

    public Function1<T1, Function1<T2, M>> curry() {
        return new Function1<T1, Function1<T2, M>>() {
            @Override
            public Function1<T2, M> apply(T1 arg1) {
                return bind1(arg1);
            }
        };
    }
}
