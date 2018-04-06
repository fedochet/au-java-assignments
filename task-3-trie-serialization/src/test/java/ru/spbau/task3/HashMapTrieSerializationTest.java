package ru.spbau.task3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

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
}
