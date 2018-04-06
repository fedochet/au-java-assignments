package ru.spbau.task3;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

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

    @Test
    void tries_with_same_content_are_equal() {
        List<String> words = Arrays.asList("hello", "world", "one", "two", "three");
        HashMapTrie trie1 = new HashMapTrie();
        HashMapTrie trie2 = new HashMapTrie();

        words.forEach(word -> {
            trie1.add(word);
            trie2.add(word);
        });

        assertThat(trie1).isEqualTo(trie2);
        assertThat(trie2).isEqualTo(trie1);
    }

    @Test
    void tries_with_one_different_word_are_not_equal() {
        List<String> words1 = Arrays.asList("hello", "world", "one", "two", "three");
        List<String> words2 = Arrays.asList("hello", "world", "one", "two", "three", "four");
        HashMapTrie trie1 = new HashMapTrie();
        HashMapTrie trie2 = new HashMapTrie();

        words1.forEach(trie1::add);
        words2.forEach(trie2::add);

        assertThat(trie1).isNotEqualTo(trie2);
        assertThat(trie2).isNotEqualTo(trie1);
    }

    @Test
    void tries_with_different_words_are_not_equal() {
        List<String> words1 = Arrays.asList("one", "two", "three");
        List<String> words2 = Arrays.asList("four", "five", "six");
        HashMapTrie trie1 = new HashMapTrie();
        HashMapTrie trie2 = new HashMapTrie();

        words1.forEach(trie1::add);
        words2.forEach(trie2::add);

        assertThat(trie1).isNotEqualTo(trie2);
        assertThat(trie2).isNotEqualTo(trie1);
    }

    @Test
    void equal_tries_are_not_equal_after_removing_and_equal_after_addition() {
        List<String> words = Arrays.asList("one", "two", "three");
        HashMapTrie trie1 = new HashMapTrie();
        HashMapTrie trie2 = new HashMapTrie();

        words.forEach(trie1::add);
        words.forEach(trie2::add);

        assertThat(trie1).isEqualTo(trie2);
        assertThat(trie2).isEqualTo(trie1);

        trie1.add("four");

        assertThat(trie1).isNotEqualTo(trie2);
        assertThat(trie2).isNotEqualTo(trie1);

        trie2.add("four");

        assertThat(trie1).isEqualTo(trie2);
        assertThat(trie2).isEqualTo(trie1);
    }
}
