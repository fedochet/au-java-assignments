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

        assertThat(dict.getInternalState().getBuckets()).hasSize(capacity);

        dict.put(6, 6);

        assertThat(dict.getInternalState().getBuckets()).hasSize(capacity * 2);
        assertThat(dict.size()).isEqualTo(6);
    }

    @Test
    @DisplayName("Capacity is halved on removal when `capacity` * `loadFactor` is greater than `size` * 2")
    void capacity_is_decreased_when_size_is_small() {
        int capacity = 10;
        double loadFactor = 0.8;
        HashDictionary<Integer, Integer> dict = new HashDictionary<>(capacity, loadFactor);

        dict.put(1, 1);
        dict.put(2, 2);
        dict.put(3, 3);
        dict.put(4, 4);

        assertThat(dict.getInternalState().getBuckets()).hasSize(capacity);

        dict.remove(1);

        assertThat(dict.getInternalState().getBuckets()).hasSize(capacity / 2);
        assertThat(dict.size()).isEqualTo(3);
    }

    @Test
    void no_rehashing_happens_on_inserting_by_null_key() {
        int capacity = 3;
        double loadFactor = 0.5;
        HashDictionary<Integer, Integer> dict = new HashDictionary<>(capacity, loadFactor);

        dict.put(1, 1);
        dict.put(null, 1);

        assertThat(dict.getInternalState().getBuckets()).hasSize(capacity);
        assertThat(dict.size()).isEqualTo(2);
    }

    @Test
    void no_rehashing_happend_on_deleting_by_null_key() {
        int capacity = 8;
        double loadFactor = 0.5;
        HashDictionary<Integer, Integer> dict = new HashDictionary<>(capacity, loadFactor);

        dict.put(1, 1);
        dict.put(null, 2);
        dict.remove(null);

        assertThat(dict.getInternalState().getBuckets()).hasSize(8);
        assertThat(dict.size()).isEqualTo(1);
    }
}
