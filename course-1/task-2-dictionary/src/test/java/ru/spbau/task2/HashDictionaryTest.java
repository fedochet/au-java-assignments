package ru.spbau.task2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HashDictionaryTest {

    private HashDictionary<String, String> dictionary = new HashDictionary<>();

    @Test
    void constructor_does_not_accept_non_positive_capacity() {
        assertThrows(IllegalArgumentException.class,
            () -> new HashDictionary<>(-1)
        );

        assertThrows(IllegalArgumentException.class,
            () -> new HashDictionary<>(0)
        );
    }

    @Test
    void constructor_does_not_accept_non_positive_loadFactor() {
        assertThrows(IllegalArgumentException.class,
            () -> new HashDictionary<>(1, 0)
        );

        assertThrows(IllegalArgumentException.class,
            () -> new HashDictionary<>(1, -1)
        );
    }

    @Test
    void new_dictionary_has_zero_size() {
        assertThat(dictionary.size()).isEqualTo(0);
    }

    @Test
    void new_dictionary_does_not_contains_elements() {
        assertThat(dictionary.contains("string")).isFalse();
    }


    @Test
    void if_element_is_inserted_for_the_first_time_then_put_returns_null() {
        assertThat(dictionary.put("key", "value")).isNull();
    }

    @Test
    void if_element_did_not_existed_then_delete_returns_null() {
        assertThat(dictionary.remove("key")).isNull();
    }

    @Test
    void if_element_existed_then_it_is_returned_after_deletion() {
        dictionary.put("key", "value");

        assertThat(dictionary.remove("key")).isEqualTo("value");
    }

    @Test
    void if_element_was_already_present_then_old_value_is_returned() {
        dictionary.put("key", "oldValue");

        assertThat(dictionary.put("key", "newValue")).isEqualTo("oldValue");
    }

    @Test
    void if_element_is_not_in_dict_then_it_is_not_contained() {
        assertThat(dictionary.contains("key")).isFalse();
    }

    @Test
    void after_putting_element_is_is_contained_in_dict() {
        dictionary.put("key", "value");

        assertThat(dictionary.contains("key")).isTrue();
    }

    @Test
    void after_removal_element_is_not_contained_in_dict() {
        dictionary.put("key", "value");

        dictionary.remove("key");

        assertThat(dictionary.contains("key")).isFalse();
    }

    @Test
    void size_is_increased_after_addition() {
        dictionary.put("key1", "value");
        assertThat(dictionary.size()).isEqualTo(1);

        dictionary.put("key2", "value");
        assertThat(dictionary.size()).isEqualTo(2);

        dictionary.put("key3", "value");
        assertThat(dictionary.size()).isEqualTo(3);
    }

    @Test
    void size_is_decreased_after_deletion() {
        dictionary.put("key1", "value");
        dictionary.put("key2", "value");
        dictionary.put("key3", "value");

        dictionary.remove("key1");
        assertThat(dictionary.size()).isEqualTo(2);

        dictionary.remove("key2");
        assertThat(dictionary.size()).isEqualTo(1);

        dictionary.remove("key3");
        assertThat(dictionary.size()).isEqualTo(0);
    }

    @Test
    void after_clear_size_is_zero() {
        dictionary.put("key1", "value");
        dictionary.put("key2", "value");
        dictionary.put("key3", "value");
        dictionary.put("key4", null);
        dictionary.put(null, null);

        dictionary.clear();

        assertThat(dictionary.size()).isEqualTo(0);

        assertThat(dictionary.contains("key1")).isFalse();
        assertThat(dictionary.contains("key2")).isFalse();
        assertThat(dictionary.contains("key3")).isFalse();
        assertThat(dictionary.contains("key4")).isFalse();
        assertThat(dictionary.contains(null)).isFalse();

    }

    @Test
    void get_by_non_existing_key_returns_null() {
        assertThat(dictionary.get("key")).isNull();
    }

    @Test
    void after_inserting_value_can_be_queried_by_key() {
        dictionary.put("key", "value");

        assertThat(dictionary.get("key")).isEqualTo("value");
    }

    @Test
    void many_elements_can_by_queried_by_their_keys() {
        dictionary.put("key1", "value1");
        dictionary.put("key2", "value2");
        dictionary.put("key3", "value3");

        assertThat(dictionary.get("key1")).isEqualTo("value1");
        assertThat(dictionary.get("key2")).isEqualTo("value2");
        assertThat(dictionary.get("key3")).isEqualTo("value3");
    }

    @Nested
    @DisplayName("Tests for correct work with null keys and values")
    class NullKeyAndValueTests {
        @Test
        void dict_accepts_null_key() {
            dictionary.put(null, "value");

            assertThat(dictionary.get(null)).isEqualTo("value");
            assertThat(dictionary.contains(null)).isTrue();
            assertThat(dictionary.size()).isEqualTo(1);
        }

        @Test
        void dict_accepts_null_value() {
            dictionary.put("key", null);

            assertThat(dictionary.get("key")).isNull();
            assertThat(dictionary.contains("key")).isTrue();
            assertThat(dictionary.size()).isEqualTo(1);
        }

        @Test
        void dict_accepts_null_key_and_value() {
            dictionary.put(null, null);

            assertThat(dictionary.get(null)).isNull();
            assertThat(dictionary.contains(null)).isTrue();
            assertThat(dictionary.size()).isEqualTo(1);
        }

        @Test
        void inserting_by_null_key_again_overwrites_value() {
            dictionary.put(null, "value1");

            String oldValue = dictionary.put(null, "value2");

            assertThat(oldValue).isEqualTo("value1");
            assertThat(dictionary.get(null)).isEqualTo("value2");
            assertThat(dictionary.contains(null)).isTrue();
        }

        @Test
        void removing_by_null_key_works() {
            dictionary.put(null, "value");

            assertThat(dictionary.remove(null)).isEqualTo("value");
            assertThat(dictionary.contains(null)).isFalse();
            assertThat(dictionary.size()).isEqualTo(0);
        }
    }

    @Test
    void different_keys_with_same_hash_are_stored_correctly() {
        HashDictionary<Object, String> dict = new HashDictionary<>();

        int hashCodeValue = 1;
        Object key1 = objectWithHashCode(hashCodeValue);
        Object key2 = objectWithHashCode(hashCodeValue);

        dict.put(key1, "value1");
        dict.put(key2, "value2");

        assertThat(dict.get(key1)).isEqualTo("value1");
        assertThat(dict.get(key2)).isEqualTo("value2");
    }

    private Object objectWithHashCode(int hashCodeValue) {
        return new Object() {
            @Override
            public int hashCode() {
                return hashCodeValue;
            }
        };
    }
}