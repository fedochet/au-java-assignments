package ru.spbau.task4;

import java.util.ArrayList;
import java.util.Iterator;

public final class Collections {
    private Collections() {
    }

    public static <T, R> Iterable<R> map(Iterable<? extends T> iterable, Function1<? super T, ? extends R> mapper) {
        return foldl(
            iterable,
            (acc, t) -> append(acc, mapper.apply(t)),
            new ArrayList<>()
        );
    }

    public static <T> Iterable<T> filter(Iterable<? extends T> iterable, Predicate<? super T> condition) {
        return foldl(
            iterable,
            (acc, t) -> condition.apply(t) ? append(acc, t) : acc,
            new ArrayList<>()
        );
    }

    public static <T, U> U foldr(Iterable<T> iterable, Function2<T, U, U> f, U zero) {
        return recursiveFoldr(iterable.iterator(), f, zero);
    }

    public static <T, U> U foldl(Iterable<? extends T> iterable, Function2<? super U, ? super T, ? extends U> f, U zero) {
        U result = zero;
        for (T t : iterable) {
            result = f.apply(result, t);
        }

        return result;
    }

    private static <U, T> U recursiveFoldr(Iterator<T> iterator, Function2<T, U, U> f, U zero) {
        if (iterator.hasNext()) {
            T current = iterator.next();
            U rest = recursiveFoldr(iterator, f, zero);
            return f.apply(current, rest);
        }

        return zero;
    }

    private static <R> ArrayList<R> append(ArrayList<R> acc, R elem) {
        acc.add(elem);
        return acc;
    }
}
