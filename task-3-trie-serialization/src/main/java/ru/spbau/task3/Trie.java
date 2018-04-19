package ru.spbau.task3;

import org.jetbrains.annotations.NotNull;

public interface Trie {

    /**
     * Expected complexity: O(|element|)
     * @return <tt>true</tt> if this set did not already contain the specified
     *         element
     */
    boolean add(@NotNull String element);

    /**
     * Expected complexity: O(|element|)
     * @return <tt>true</tt> if this set contains specified element
     */
    boolean contains(@NotNull String element);

    /**
     * Expected complexity: O(|element|)
     * @return <tt>true</tt> if this set contained the specified element
     */
    boolean remove(@NotNull String element);

    /**
     * Expected complexity: O(1)
     */
    int size();

    /**
     * Expected complexity: O(|prefix|)
     */
    int howManyStartsWithPrefix(@NotNull String prefix);
}
