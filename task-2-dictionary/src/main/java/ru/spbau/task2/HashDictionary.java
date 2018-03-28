package ru.spbau.task2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;

public class HashDictionary<K, V> implements Dictionary<K, V> {

    private final double LOAD_FACTOR;
    private int BUCKETS_NUMBER;
    private int size = 0;

    private final List<HashDictionaryBucket<K, V>> buckets = new ArrayList<>();

    @Nullable
    private V nullEntry = null;
    private boolean containsNullEntry = false;

    public HashDictionary(int capacity, double loadFactor) {
        BUCKETS_NUMBER = capacity;
        LOAD_FACTOR = loadFactor;
        createBuckets();
    }

    public HashDictionary(int capacity) {
        this(capacity, 0.75);
    }

    public HashDictionary() {
        this(128);
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

        return getBucketByKey(key).find(key) != null;
    }

    @Nullable
    @Override
    public V get(@Nullable K key) {
        if (key == null) {
            return nullEntry;
        }

        BucketEntry<K, V> entry = getBucketByKey(key).find(key);
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

        HashDictionaryBucket<K, V> bucket = getBucketByKey(key);
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

        BucketEntry<K, V> entry = getBucketByKey(key).remove(key);

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

    private HashDictionaryBucket<K, V> getBucketByKey(K key) {
        int bucketIndex = (key.hashCode() % BUCKETS_NUMBER + BUCKETS_NUMBER) % BUCKETS_NUMBER;
        return buckets.get(bucketIndex);
    }

    /**
     * This method is for testing purposes only; don't use it in production.
     *
     * @return internal state object that is binded to this dictionary.
     */
    @TestOnly
    InternalState getInternalState() {
        return new InternalState();
    }

    /**
     * This class allows introspection of dictionary internal state.
     *
     * For testing purposes only!
     */
    @TestOnly
    class InternalState {
        int getBucketsNumber() {
            return BUCKETS_NUMBER;
        }

        List<HashDictionaryBucket<K, V>> getBuckets() {
            return buckets;
        }

        HashDictionaryBucket<K, V> getBucketByKey(@NotNull K key) {
            return HashDictionary.this.getBucketByKey(key);
        }
    }

}
