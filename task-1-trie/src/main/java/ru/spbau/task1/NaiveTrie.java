package ru.spbau.task1;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class NaiveTrie implements Trie {

    private static final Pattern IS_ALPHABETIC = Pattern.compile("[a-zA-Z]+");

    private final Set<String> strings = new HashSet<>();

    @Override
    public boolean add(String element) {
        return strings.add(assertIsAlpbhabetic(element));
    }

    @Override
    public boolean contains(String element) {
        return strings.contains(assertIsAlpbhabetic(element));
    }

    @Override
    public boolean remove(String element) {
        return strings.remove(assertIsAlpbhabetic(element));
    }

    @Override
    public int size() {
        return strings.size();
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        assertIsAlpbhabetic(prefix);
        return 0;
    }

    private String assertIsAlpbhabetic(String element) {
        if (!IS_ALPHABETIC.matcher(element).matches()) {
            throw new IllegalArgumentException(MessageFormat.format("Cannot accept {0} because it is not alphabetic string.", element));
        }

        return element;
    }
}
