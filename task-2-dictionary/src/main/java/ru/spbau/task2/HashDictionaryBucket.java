package ru.spbau.task2;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

class HashDictionaryBucket<V, K> {
    private final List<BucketEntry<V, K>> entries = new LinkedList<>();

    @Nonnull
    Optional<V> find(K key) {
        return entries.stream()
            .filter(entry -> entry.key.equals(key))
            .map(entry -> entry.value)
            .findFirst();
    }

//    Optional<V> insert(K key, V value) {}

    private static class BucketEntry<T, K> {
        final T key;
        final T value;

        BucketEntry(@Nonnull T key, @Nonnull T value) {
            this.key = key;
            this.value = value;
        }
    }
}
