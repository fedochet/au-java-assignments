package ru.spbau.task2;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class HashDictionary<K, V> implements Dictionary<K, V> {
    private final Map<K, V> hashMap = new HashMap<>();

    @Override
    public int size() {
        return hashMap.size();
    }

    @Override
    public boolean contains(@Nullable K key) {
        return hashMap.containsKey(key);
    }

    @Nullable
    @Override
    public V get(@Nullable K key) {
        return hashMap.get(key);
    }

    @Nullable
    @Override
    public V put(@Nullable K key, @Nullable V value) {
        return hashMap.put(key, value);
    }

    @Nullable
    @Override
    public V remove(@Nullable K key) {
        return hashMap.remove(key);
    }

    @Override
    public void clear() {
        hashMap.clear();
    }
}
