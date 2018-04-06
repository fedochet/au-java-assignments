package ru.spbau.task3;

import java.text.MessageFormat;
import java.util.regex.Pattern;

public class HashMapTrie implements Trie {

    private static final Pattern IS_ALPHABETIC = Pattern.compile("[a-zA-Z]+");

    private final HashMapTrieNode trieNode = new HashMapTrieNode();

    @Override
    public boolean add(String element) {
        return trieNode.add(assertIsAlpbhabetic(element), 0);
    }

    @Override
    public boolean contains(String element) {
        return trieNode.contains(assertIsAlpbhabetic(element), 0);
    }

    @Override
    public boolean remove(String element) {
        return trieNode.remove(assertIsAlpbhabetic(element), 0);
    }

    @Override
    public int size() {
        return trieNode.size();
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        HashMapTrie that = (HashMapTrie) obj;

        return trieNode.equals(that.trieNode);
    }
}
