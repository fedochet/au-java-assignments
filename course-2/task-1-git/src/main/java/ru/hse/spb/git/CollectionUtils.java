package ru.hse.spb.git;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final public class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> Stream<T> generateStream(T seed, Function<T, T> next) {
        return toStream(iterate(seed, next));
    }

    private static <T> Iterator<T> iterate(@Nullable T seed, @NotNull Function<T, T> next) {
        return new Iterator<T>() {
            T current = seed;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                T previous = this.current;
                this.current = next.apply(previous);

                return previous;
            }
        };
    }

    public static <T> Stream<T> toStream(Iterator<T> iterator) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
            false
        );
    }

    public interface IOPredicate<T> {
        boolean test(T t) throws IOException;
    }

    public interface IOFunction<F, T> {
        T apply(F t) throws IOException;
    }

    public static <T> Predicate<T> ioPredicate(IOPredicate<T> p) {
        return t -> {
            try {
                return p.test(t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <F, T> Function<F, T> ioFunction(IOFunction<F, T> p) {
        return t -> {
            try {
                return p.apply(t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
