package ru.spbau.task2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HashMapTrieTest {
    private Trie trie = new HashMapTrie();

    @Nested
    @DisplayName("test newly created HashMapTrie")
    class NewTrieTest {

        @Test
        void new_trie_has_zero_size() {
            assertThat(trie.size()).isEqualTo(0);
        }

        @Test
        void new_trie_does_not_contain_anything() {
            assertThat(trie.contains("anything")).isFalse();
        }

        @Test
        void nothing_can_be_deleted_from_new_trie() {
            assertThat(trie.remove("anything")).isFalse();
        }

        @Test
        void nothing_starts_with_some_prefix_in_new_trie() {
            assertThat(trie.howManyStartsWithPrefix("anything")).isEqualTo(0);
        }
    }

    @DisplayName("trie should not accept non-alphabetical element for addition")
    @ParameterizedTest
    @ValueSource(strings = {"", "with space", "with.dot", "%%odd^&"})
    void trie_does_not_accept_non_alphabetical_strings(String element) {
        assertThrows(IllegalArgumentException.class, () -> trie.add(element));
        assertThrows(IllegalArgumentException.class, () -> trie.contains(element));
        assertThrows(IllegalArgumentException.class, () -> trie.remove(element));
        assertThrows(IllegalArgumentException.class, () -> trie.howManyStartsWithPrefix(element));
    }

    @Test
    void trie_does_not_accept_null() {
        assertThrows(IllegalArgumentException.class, () -> trie.add(null));
        assertThrows(IllegalArgumentException.class, () -> trie.contains(null));
        assertThrows(IllegalArgumentException.class, () -> trie.remove(null));
        assertThrows(IllegalArgumentException.class, () -> trie.howManyStartsWithPrefix(null));
    }

    @Test
    void trie_returns_true_when_adding_element_that_didnt_existed() {
        assertThat(trie.add("element")).isTrue();
    }

    @Test
    void trie_rejects_inserting_same_element_twice() {
        trie.add("element");

        assertThat(trie.add("element")).isFalse();
    }

    @Test
    void trie_contains_element_after_it_was_added() {
        trie.add("element");

        assertThat(trie.contains("element")).isTrue();
    }

    @Test
    void trie_returns_true_from_delete_if_element_was_in_it() {
        trie.add("element");

        assertThat(trie.remove("element")).isTrue();
    }

    @Test
    void trie_does_not_contains_element_after_its_deletion() {
        trie.add("element");
        trie.remove("element");

        assertThat(trie.contains("element")).isFalse();
    }

    @Test
    void trie_rejects_deleting_same_element_twice() {
        trie.add("element");
        trie.remove("element");

        assertThat(trie.remove("element")).isFalse();
    }

    @Test
    void trie_size_increases_after_adding_elements() {
        trie.add("one");
        assertThat(trie.size()).isEqualTo(1);

        trie.add("two");
        assertThat(trie.size()).isEqualTo(2);

        trie.add("three");
        assertThat(trie.size()).isEqualTo(3);
    }

    @Test
    void trie_size_is_not_changed_when_same_elements_added() {
        trie.add("one");
        trie.add("one");
        trie.add("two");
        trie.add("two");

        assertThat(trie.size()).isEqualTo(2);
    }

    @Test
    void trie_size_decreases_after_successful_deletions() {
        trie.add("one");
        trie.add("two");
        trie.add("three");

        trie.remove("one");
        assertThat(trie.size()).isEqualTo(2);
        trie.remove("two");
        assertThat(trie.size()).isEqualTo(1);
        trie.remove("three");
        assertThat(trie.size()).isEqualTo(0);
    }

    @Test
    void deleting_of_full_prefix_works_correctly() {
        trie.add("hello");
        trie.add("helloween");

        assertThat(trie.remove("hello")).isTrue();
        assertThat(trie.size()).isEqualTo(1);
        assertThat(trie.contains("helloween"));
        assertThat(trie.contains("hello")).isFalse();
    }

    @Test
    void deleting_of_full_prefix_when_it_doesnt_exist_works_correctly() {
        trie.add("helloween");

        assertThat(trie.remove("hello")).isFalse();
        assertThat(trie.size()).isEqualTo(1);
        assertThat(trie.contains("helloween"));
    }

    @Test
    void element_counts_for_each_of_it_prefixes() {
        trie.add("element");

        assertThat(trie.howManyStartsWithPrefix("e")).isEqualTo(1);
        assertThat(trie.howManyStartsWithPrefix("el")).isEqualTo(1);
        assertThat(trie.howManyStartsWithPrefix("ele")).isEqualTo(1);
        assertThat(trie.howManyStartsWithPrefix("elem")).isEqualTo(1);
        assertThat(trie.howManyStartsWithPrefix("eleme")).isEqualTo(1);
        assertThat(trie.howManyStartsWithPrefix("elemen")).isEqualTo(1);
        assertThat(trie.howManyStartsWithPrefix("element")).isEqualTo(1);
    }

    @Test
    void different_elements_are_both_counted_by_same_prefix() {
        trie.add("hello");
        trie.add("hexagram");

        assertThat(trie.howManyStartsWithPrefix("h")).isEqualTo(2);
        assertThat(trie.howManyStartsWithPrefix("he")).isEqualTo(2);
    }

    @Test
    void after_removing_one_element_prefix_count_decreased_only_by_one() {
        trie.add("hello");
        trie.add("hexagram");
        trie.remove("hello");

        assertThat(trie.howManyStartsWithPrefix("h")).isEqualTo(1);
        assertThat(trie.howManyStartsWithPrefix("he")).isEqualTo(1);
    }

    @Test
    void prefix_count_correctly_works_for_branching() {
        trie.add("hello");
        trie.add("hell");
        trie.add("helicopter");
        trie.add("hero");
        trie.add("horror");
        trie.add("else");

        assertThat(trie.size()).isEqualTo(6);
        assertThat(trie.howManyStartsWithPrefix("h")).isEqualTo(5);
        assertThat(trie.howManyStartsWithPrefix("he")).isEqualTo(4);
        assertThat(trie.howManyStartsWithPrefix("hel")).isEqualTo(3);
        assertThat(trie.howManyStartsWithPrefix("hell")).isEqualTo(2);
        assertThat(trie.howManyStartsWithPrefix("hello")).isEqualTo(1);
    }

    @Test
    void prefix_distinguish_between_upper_and_lower_case() {
        trie.add("hello");
        trie.add("Hello");

        assertThat(trie.howManyStartsWithPrefix("H")).isEqualTo(1);
        assertThat(trie.howManyStartsWithPrefix("h")).isEqualTo(1);
    }
}