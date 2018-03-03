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
        return strings.add(assertElement(element));
    }

    @Override
    public boolean contains(String element) {
        return strings.contains(element);
    }

    @Override
    public boolean remove(String element) {
        return strings.remove(element);
    }

    @Override
    public int size() {
        return strings.size();
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        return 0;
    }

    private String assertElement(String element) {
        if (!IS_ALPHABETIC.matcher(element).matches()) {
            throw new IllegalArgumentException(MessageFormat.format("Cannot accept {0} because it is not alphabetic string.", element));
        }

        return element;
    }
}
