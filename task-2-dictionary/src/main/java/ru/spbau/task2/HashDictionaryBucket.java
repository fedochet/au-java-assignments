package ru.spbau.task2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class HashDictionaryBucket<K, V> {
    private final List<BucketEntry<K, V>> entries = new LinkedList<>();

    /**
     * Attempts to find entry by its key.
     *
     * @param key key of the entry.
     * @return entry if it existed; null otherwise.
     */
    @Nullable
    BucketEntry<K, V> find(K key) {
        for (BucketEntry<K, V> entry : entries) {
            if (entry.key.equals(key)) {
                return entry;
            }
        }

        return null;
    }

    /**
     * Attempts to insert entry with key and value; if entry with such key already exists,
     * replaces its value by the new one.
     *
     * @param key key for entry
     * @param value value for entry
     * @return value that was previously stored by that key (may be null), or null if there was no such key in this
     * bucket
     */
    @Nullable
    V insert(@NotNull K key, @Nullable V value) {
        BucketEntry<K, V> entry = find(key);
        if (entry == null) {
            addNewEntry(key, value);
            return null;
        }

        V oldVal = entry.value;
        entry.value = value;

        return oldVal;
    }

    /**
     * Attempts to remove entry by key.
     *
     * @param key key of entry to remove.
     * @return value of entry if it was found; null otherwise.
     */
    @Nullable
    V remove(@NotNull K key) {
        for (Iterator<BucketEntry<K, V>> iterator = entries.iterator(); iterator.hasNext(); ) {
            BucketEntry<K, V> entry = iterator.next();
            if (entry.key == key) {
                iterator.remove();
                return entry.value;
            }
        }

        return null;
    }

    /**
     * @return number of stored entries.
     */
    int size() {
        return entries.size();
    }

    private void addNewEntry(@NotNull K key, @Nullable V value) {
        entries.add(new BucketEntry<>(key, value));
    }

    static class BucketEntry<K, V> {
        @NotNull
        final K key;

        @Nullable
        V value;

        BucketEntry(@NotNull K key, @Nullable V value) {
            this.key = key;
            this.value = value;
        }
    }
}
