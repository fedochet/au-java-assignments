package ru.spbau.task4;

public abstract class Predicate<T> extends Function1<T, Boolean> {
    Predicate<T> or(Predicate<T> other) {
        Predicate<T> that = this;
        return new Predicate<T>() {
            @Override
            public Boolean apply(T arg) {
                return that.apply(arg) || other.apply(arg);
            }
        };
    }

    Predicate<T> and(Predicate<T> other) {
        Predicate<T> that = this;
        return new Predicate<T>() {
            @Override
            public Boolean apply(T arg) {
                return that.apply(arg) && other.apply(arg);
            }
        };
    }
}
