package ru.spbau.task2;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashDictionaryInternalTest {


    @Test
    void after_creation_dictionary_has_number_of_buckets_equal_to_capacity() {
        int capacity = 100;
        HashDictionary<String, String> newDict = new HashDictionary<>(capacity);

        assertThat(newDict.getInternalState().getBuckets()).hasSize(capacity);
    }

    @Test
    @DisplayName("Capacity is doubled when `capacity` * `loadFactor` is less than `size`")
    void capacity_is_doubled_when_capacity_is_less_than_size_by_loadFactor() {
        int capacity = 10;
        double loadFactor = 0.5;
        HashDictionary<Integer, Integer> dict = new HashDictionary<>(capacity, loadFactor);

        dict.put(1, 1);
        dict.put(2, 2);
        dict.put(3, 3);
        dict.put(4, 4);

        dict.put(5, 5);

        assertThat(dict.getInternalState().getBuckets())
            .hasSize(capacity * 2);
    }
}
