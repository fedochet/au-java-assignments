package ru.spbau.task2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BucketEntry<K, V> {
    @NotNull
    final K key;

    @Nullable
    final V value;

    BucketEntry(@NotNull K key, @Nullable V value) {
        this.key = key;
        this.value = value;
    }
}
