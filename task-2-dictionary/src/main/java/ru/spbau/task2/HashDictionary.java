package ru.spbau.task2;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;

public class HashDictionary<K, V> implements Dictionary<K, V> {

    private static final int EXPANSION_FACTOR = 2;

    private final double LOAD_FACTOR;
    private int BUCKETS_NUMBER;
    private int size = 0;

    private final List<HashDictionaryBucket<K, V>> buckets = new ArrayList<>();

    @Nullable
    private V nullEntry = null;
    private boolean containsNullEntry = false;

    public HashDictionary(int capacity, double loadFactor) {
        assertParameters(capacity, loadFactor);

        BUCKETS_NUMBER = capacity;
        LOAD_FACTOR = loadFactor;

        createBuckets();
    }

    public HashDictionary(int capacity) {
        this(capacity, 0.75);
    }

    public HashDictionary() {
        this(16);
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
        if (size > BUCKETS_NUMBER * LOAD_FACTOR) {
            expandUp();
        }

        return null;
    }

    @Nullable
    @Override
    public V remove(@Nullable K key) {
        if (key == null) {
            return removeByNullKey();
        }

        BucketEntry<K, V> entry = getBucketByKey(key).remove(key);

        if (entry == null) {
            return null;
        }

        size--;
        if (EXPANSION_FACTOR * size < BUCKETS_NUMBER * LOAD_FACTOR) {
            shrinkDown();
        }

        return entry.value;
    }

    @Override
    public void clear() {
        buckets.clear();
        removeNullEntry();

        size = 0;

        createBuckets();
    }

    private void assertParameters(int capacity, double loadFactor) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity should be positive; got " + capacity);
        }

        if (loadFactor <= 0) {
            throw new IllegalArgumentException("Load factor should be positive; got " + loadFactor);
        }
    }

    private void createBuckets() {
        for (int i = 0; i < BUCKETS_NUMBER; i++) {
            buckets.add(new HashDictionaryBucket<>());
        }
    }

    private void removeNullEntry() {
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

    private void shrinkDown() {
        BUCKETS_NUMBER /= EXPANSION_FACTOR;
        size = containsNullEntry ? 1 : 0;
        rehash();
    }

    private void expandUp() {
        BUCKETS_NUMBER *= EXPANSION_FACTOR;
        size = containsNullEntry ? 1 : 0;
        rehash();
    }

    private void rehash() {
        List<BucketEntry<K, V>> entries = new ArrayList<>();
        buckets.forEach(bucket -> entries.addAll(bucket.getEntries()));

        buckets.clear();
        createBuckets();

        entries.forEach(entry -> put(entry.key, entry.value));
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
     * For testing purposes only!
     */
    @TestOnly
    class InternalState {
        List<HashDictionaryBucket<K, V>> getBuckets() {
            return buckets;
        }
    }

}
