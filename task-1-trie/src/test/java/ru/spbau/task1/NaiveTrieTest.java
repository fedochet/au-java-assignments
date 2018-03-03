package ru.spbau.task1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NaiveTrieTest {
    private Trie trie = new NaiveTrie();

    @Nested
    @DisplayName("test newly created NaiveTrie")
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

    }

    @DisplayName("trie should not accept non-alphabetical element for addition")
    @ParameterizedTest
    @ValueSource(strings = {"with space", "with.dot", "%%odd^&"})
    void trie_does_not_accept_non_alphabetical_strings_for_addition(String element) {
        assertThrows(IllegalArgumentException.class, () -> {
            trie.add(element);
        });
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
    void trie_size_decreases_after_successfull_deletions() {
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
}