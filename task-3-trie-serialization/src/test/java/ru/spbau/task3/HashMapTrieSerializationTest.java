package ru.spbau.task3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HashMapTrieSerializationTest {
    private HashMapTrie originalTree = new HashMapTrie();
    private HashMapTrie deserializedTree = new HashMapTrie();
    private PipedInputStream inputStream;
    private PipedOutputStream outputStream;

    @BeforeEach
    void createPipe() throws IOException {
        inputStream = new PipedInputStream();
        outputStream = new PipedOutputStream(inputStream);
    }

    @Test
    void empty_trie_is_deserialized_to_empty_trie() throws IOException {
        originalTree.serialize(outputStream);
        deserializedTree.deserialize(inputStream);

        assertThat(deserializedTree).isEqualTo(originalTree);
    }

    @Test
    void one_element_is_saved_while_serialization() throws IOException {
        originalTree.add("hello");

        originalTree.serialize(outputStream);
        deserializedTree.deserialize(inputStream);

        assertThat(deserializedTree).isEqualTo(originalTree);
    }

    @Test
    void many_elements_are_saved_during_serialization() throws IOException {
        List<String> words = Arrays.asList("hello", "world", "one", "two", "three", "four");

        words.forEach(originalTree::add);
        originalTree.serialize(outputStream);
        deserializedTree.deserialize(inputStream);

        assertThat(deserializedTree).isEqualTo(originalTree);
    }

    @Test
    void if_exception_is_thrown_then_deserializable_trie_is_not_changed() throws IOException {
        List<String> words = Arrays.asList("hello", "world", "one", "two", "three", "four");
        List<String> wordsInDeserialized = Arrays.asList("other", "words", "from", "deserialized", "trie");

        words.forEach(originalTree::add);
        wordsInDeserialized.forEach(deserializedTree::add);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        originalTree.serialize(outputStream);

        ByteArrayInputStream brokenInputStream =
            new ByteArrayInputStream(outputStream.toByteArray(), 0, outputStream.size() / 2);

        assertThrows(
            IOException.class,
            () -> deserializedTree.deserialize(brokenInputStream)
        );

        assertThat(deserializedTree).isEqualTo(trieWithWords(wordsInDeserialized));
    }

    private HashMapTrie trieWithWords(List<String> wordsInDeserialized) {
        HashMapTrie expectedTrie = new HashMapTrie();
        wordsInDeserialized.forEach(expectedTrie::add);
        return expectedTrie;
    }
}
