package ru.spbau.task4;

import java.util.ArrayList;
import java.util.List;

public final class Collections {
    private Collections() {}

    public static <T, R> Iterable<R> map(Iterable<? extends T> iterable, Function1<? super T, ? extends R> mapper) {
        List<R> result = new ArrayList<>();
        for (T t : iterable) {
            result.add(mapper.apply(t));
        }

        return result;
    }

    public static <T> Iterable<T> filter(Iterable<? extends T> iterable, Predicate<? super T> condition) {
        List<T> result = new ArrayList<>();
        for (T t : iterable) {
            if (condition.apply(t)) {
                result.add(t);
            }
        }

        return result;
    }

//    public static <T, U> U foldr(Iterable<T> iterable, Function2<T, U, U> f, U zero) {
//        U result = zero;
//    }

    public static <T, U> U foldl(Iterable<? extends T> iterable, Function2<? super U, ? super T, ? extends U> f, U zero) {
        U result = zero;
        for (T t : iterable) {
            result = f.apply(result, t);
        }

        return result;
    }
}
