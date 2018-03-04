package ru.spbau.task1;

import java.text.MessageFormat;
import java.util.regex.Pattern;

public class NaiveTrie implements Trie {

    private static final Pattern IS_ALPHABETIC = Pattern.compile("[a-zA-Z]+");

    private TreeNode treeNode = new HashMapTreeNode();

    @Override
    public boolean add(String element) {
        return treeNode.add(assertIsAlpbhabetic(element), 0);
    }

    @Override
    public boolean contains(String element) {
        return treeNode.contains(assertIsAlpbhabetic(element), 0);
    }

    @Override
    public boolean remove(String element) {
        return treeNode.remove(assertIsAlpbhabetic(element), 0);
    }

    @Override
    public int size() {
        return treeNode.size();
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        return treeNode.countByPrefix(assertIsAlpbhabetic(prefix), 0);
    }

    private String assertIsAlpbhabetic(String element) {
        if (!IS_ALPHABETIC.matcher(element).matches()) {
            throw new IllegalArgumentException(MessageFormat.format("Cannot accept {0} because it is not alphabetic string.", element));
        }

        return element;
    }

}
