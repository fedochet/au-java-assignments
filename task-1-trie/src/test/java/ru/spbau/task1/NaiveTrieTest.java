package ru.spbau.task1;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NaiveTrieTest {
    private Trie trie = new NaiveTrie();

    @Test
    void new_trie_has_zero_size() {
        assertThat(trie.size()).isEqualTo(0);
    }
}