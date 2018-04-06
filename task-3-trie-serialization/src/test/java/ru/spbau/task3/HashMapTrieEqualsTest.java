package ru.spbau.task3;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashMapTrieEqualsTest {
    @Test
    void trie_is_equal_to_itself() {
        HashMapTrie trie = new HashMapTrie();

        assertThat(trie).isEqualTo(trie);
    }

    @Test
    void trie_is_not_equal_to_null() {
        HashMapTrie trie = new HashMapTrie();

        assertThat(trie).isNotEqualTo(null);
    }

    @Test
    void empty_tries_are_equal() {
        HashMapTrie trie1 = new HashMapTrie();
        HashMapTrie trie2 = new HashMapTrie();

        assertThat(trie1).isEqualTo(trie2);
        assertThat(trie2).isEqualTo(trie1);
    }
}
