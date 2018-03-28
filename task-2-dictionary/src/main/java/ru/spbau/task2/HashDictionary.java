package ru.spbau.task2;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HashDictionary<K, V> implements Dictionary<K, V> {

    private int BUCKETS_NUMBER = 128;
    private int size = 0;
    private final List<HashDictionaryBucket<K, V>> buckets = new ArrayList<>();

    @Nullable
    private V nullEntry = null;
    private boolean containsNullEntry = false;

    HashDictionary() {
        createBuckets();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(@Nullable K key) {
        if (key == null) {
            return containsNullEntry;
        }

        return getBucketForKey(key).find(key) != null;
    }

    @Nullable
    @Override
    public V get(@Nullable K key) {
        if (key == null) {
            return nullEntry;
        }

        BucketEntry<K, V> entry = getBucketForKey(key).find(key);
        if (entry != null) {
            return entry.value;
        }

        return null;
    }

    @Nullable
    @Override
    public V put(@Nullable K key, @Nullable V value) {
        if (key == null) {
            return putByNullKey(value);
        }

        HashDictionaryBucket<K, V> bucket = getBucketForKey(key);
        BucketEntry<K, V> previousEntry = bucket.remove(key);
        bucket.insert(key, value);

        if (previousEntry != null) {
            return previousEntry.value;
        }

        size++;
        return null;
    }

    @Nullable
    @Override
    public V remove(@Nullable K key) {
        if (key == null) {
            return removeByNullKey();
        }

        BucketEntry<K, V> entry = getBucketForKey(key).remove(key);

        if (entry != null) {
            size--;
            return entry.value;
        }

        return null;
    }

    @Override
    public void clear() {
        buckets.clear();
        clearNullEntry();

        size = 0;

        createBuckets();
    }

    private void clearNullEntry() {
        nullEntry = null;
        containsNullEntry = false;
    }

    private V putByNullKey(@Nullable V value) {
        if (containsNullEntry) {
            V previous = nullEntry;
            nullEntry = value;
            return previous;
        }

        containsNullEntry = true;
        nullEntry = value;
        size++;

        return null;
    }

    private V removeByNullKey() {
        V value = null;

        if (containsNullEntry) {
            containsNullEntry = false;
            value = nullEntry;
            size--;
        }

        return value;
    }

    private void createBuckets() {
        for (int i = 0; i < BUCKETS_NUMBER; i++) {
            buckets.add(new HashDictionaryBucket<>());
        }
    }

    private HashDictionaryBucket<K, V> getBucketForKey(K key) {
        int bucketIndex = (key.hashCode() % BUCKETS_NUMBER + BUCKETS_NUMBER) % BUCKETS_NUMBER;
        return buckets.get(bucketIndex);
    }
}
