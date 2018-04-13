package ru.spbau.task4;

public abstract class Predicate<T> extends Function1<T, Boolean> {

    private static final Predicate<?> ALWAYS_TRUE = new Predicate() {
        @Override
        public Boolean apply(Object arg) {
            return true;
        }
    };

    private static final Predicate<?> ALWAYS_FALSE = ALWAYS_TRUE.not();

    public final Predicate<T> or(Predicate<T> other) {
        Predicate<T> that = this;
        return new Predicate<T>() {
            @Override
            public Boolean apply(T arg) {
                return that.apply(arg) || other.apply(arg);
            }
        };
    }

    public final Predicate<T> and(Predicate<T> other) {
        Predicate<T> that = this;
        return new Predicate<T>() {
            @Override
            public Boolean apply(T arg) {
                return that.apply(arg) && other.apply(arg);
            }
        };
    }

    public final Predicate<T> not() {
        Predicate<T> that = this;
        return new Predicate<T>() {
            @Override
            public Boolean apply(T arg) {
                return !that.apply(arg);
            }
        };
    }

    public static <T> Predicate<T> constTrue() {
        @SuppressWarnings("unchecked")
        Predicate<T> alwaysTrue = (Predicate<T>) ALWAYS_TRUE;
        return alwaysTrue;
    }

    public static <T> Predicate<T> constFalse() {
        @SuppressWarnings("unchecked")
        Predicate<T> alwaysFalse = (Predicate<T>) ALWAYS_FALSE;
        return alwaysFalse;
    }
}
