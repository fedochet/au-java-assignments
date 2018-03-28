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
    BucketEntry<K, V> find(@NotNull K key) {
        for (BucketEntry<K, V> entry : entries) {
            if (entry.key.equals(key)) {
                return entry;
            }
        }

        return null;
    }

    /**
     * Creates new entry with key and value. Does not check for duplicates.
     *
     * @param key   key for entry
     * @param value value for entry
     */
    void insert(@NotNull K key, @Nullable V value) {
        addNewEntry(key, value);
    }

    /**
     * Attempts to remove entry by key.
     *
     * @param key key of entry to remove.
     * @return value of entry if it was found; null otherwise.
     */
    @Nullable
    BucketEntry<K, V> remove(@NotNull K key) {
        for (Iterator<BucketEntry<K, V>> iterator = entries.iterator(); iterator.hasNext(); ) {
            BucketEntry<K, V> entry = iterator.next();
            if (entry.key == key) {
                iterator.remove();
                return entry;
            }
        }

        return null;
    }

    private void addNewEntry(@NotNull K key, @Nullable V value) {
        entries.add(new BucketEntry<>(key, value));
    }

}
