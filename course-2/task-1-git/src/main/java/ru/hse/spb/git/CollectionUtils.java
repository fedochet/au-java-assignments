package ru.hse.spb.git;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final public class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> Stream<T> generateStream(T seed, Function<T, Optional<T>> next) {
        return toStream(iterate(seed, next));
    }

    private static <T> Iterator<T> iterate(@Nullable T seed, @NotNull Function<T, Optional<T>> next) {
        return new Iterator<T>() {
            T current = seed;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                T previous = this.current;
                this.current = next.apply(previous).orElse(null);

                return previous;
            }
        };
    }

    private static <T> Stream<T> toStream(Iterator<T> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
