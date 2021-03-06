package ru.spbau.task3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.regex.Pattern;

public class HashMapTrie implements Trie, StreamSerializable {

    private static final Pattern IS_ALPHABETIC = Pattern.compile("[a-zA-Z]+");

    private final HashMapTrieNode trieNode = new HashMapTrieNode();

    @Override
    public boolean add(@NotNull String element) {
        return trieNode.add(assertIsAlpbhabetic(element), 0);
    }

    @Override
    public boolean contains(@NotNull String element) {
        return trieNode.contains(assertIsAlpbhabetic(element), 0);
    }

    @Override
    public boolean remove(@NotNull String element) {
        return trieNode.remove(assertIsAlpbhabetic(element), 0);
    }

    @Override
    public int size() {
        return trieNode.size();
    }

    @Override
    public int howManyStartsWithPrefix(@NotNull String prefix) {
        return trieNode.countByPrefix(assertIsAlpbhabetic(prefix), 0);
    }

    private String assertIsAlpbhabetic(String element) {
        if (element == null || !IS_ALPHABETIC.matcher(element).matches()) {
            throw new IllegalArgumentException(
                MessageFormat.format("Cannot accept {0} because it is not alphabetic string.", element));
        }

        return element;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        HashMapTrie that = (HashMapTrie) obj;

        return trieNode.equals(that.trieNode);
    }

    @Override
    public void serialize(@NotNull OutputStream out) throws IOException {
        Objects.requireNonNull(out, "Output stream cannot be null.");
        trieNode.serialize(out);
    }

    @Override
    public void deserialize(@NotNull InputStream in) throws IOException {
        Objects.requireNonNull(in, "Input stream cannot be null.");
        trieNode.deserialize(in);
    }
}
