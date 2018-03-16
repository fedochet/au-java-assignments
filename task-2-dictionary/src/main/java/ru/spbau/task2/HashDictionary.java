package ru.spbau.task2;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class HashDictionary implements Dictionary {
    private final Map<String, String> hashMap = new HashMap<>();

    @Override
    public int size() {
        return hashMap.size();
    }

    @Override
    public boolean contains(@Nullable String key) {
        return hashMap.containsKey(key);
    }

    @Override
    @Nullable
    public String get(@Nullable String key) {
        return hashMap.get(key);
    }

    @Override
    @Nullable
    public String put(@Nullable String key, @Nullable String value) {
        return hashMap.put(key, value);
    }

    @Override
    public String remove(@Nullable String key) {
        return hashMap.remove(key);
    }

    @Override
    public void clear() {
        hashMap.clear();
    }
}
