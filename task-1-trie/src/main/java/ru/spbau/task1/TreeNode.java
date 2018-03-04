package ru.spbau.task1;

interface TreeNode {
    boolean add(String element, int fromPosition);

    boolean contains(String element, int fromPosition);

    boolean remove(String element, int fromPosition);

    int size();

    int countByPrefix(String prefix, int fromPosition);
}
