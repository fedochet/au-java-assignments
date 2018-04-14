package ru.spbau.task4;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public final class Collections {
    private Collections() {
    }

    public static <T, U> U foldr(Iterable<? extends T> iterable, Function2<? super T, ? super U, ? extends U> f, U zero) {
        return recursiveFoldr(iterable.iterator(), f, zero);
    }

    /**
     * See <a href='https://wiki.haskell.org/Foldl_as_foldr'>https://wiki.haskell.org/Foldl_as_foldr</a>.
     */
    public static <T, U> U foldl(Iterable<? extends T> iterable, Function2<? super U, ? super T, ? extends U> f, U zero) {
        Function1<U, U> foldlWithFoldr = foldr(iterable, (b, g) -> (x) -> g.apply(f.apply(x, b)), x -> x);
        return foldlWithFoldr.apply(zero);
    }

    public static <T, R> Iterable<R> map(Iterable<? extends T> iterable, Function1<? super T, ? extends R> mapper) {
        return foldl(
            iterable,
            (acc, t) -> pushBack(acc, mapper.apply(t)),
            new ArrayList<>()
        );
    }

    public static <T> Iterable<T> filter(Iterable<? extends T> iterable, Predicate<? super T> condition) {
        return foldl(
            iterable,
            (acc, t) -> condition.apply(t) ? pushBack(acc, t) : acc,
            new ArrayList<>()
        );
    }

    public static <T> Iterable<T> takeWhile(Iterable<? extends T> iterable, Predicate<? super T> condition) {
        return foldr(
            iterable,
            (t, acc) -> condition.apply(t) ? pushFront(acc, t) : new LinkedList<>(),
            new LinkedList<>()
        );
    }

    public static <T> Iterable<T> takeUnless(Iterable<? extends T> iterable, Predicate<? super T> condition) {
        return takeWhile(iterable, condition.not());
    }

    private static <U, T> U recursiveFoldr(Iterator<? extends T> iterator, Function2<? super T, ? super U, ? extends U> f, U zero) {
        if (iterator.hasNext()) {
            T current = iterator.next();
            U rest = recursiveFoldr(iterator, f, zero);
            return f.apply(current, rest);
        }

        return zero;
    }

    private static <R> ArrayList<R> pushBack(ArrayList<R> acc, R elem) {
        acc.add(elem);
        return acc;
    }

    private static <R> LinkedList<R> pushFront(LinkedList<R> acc, R elem) {
        acc.add(0, elem);
        return acc;
    }

}
