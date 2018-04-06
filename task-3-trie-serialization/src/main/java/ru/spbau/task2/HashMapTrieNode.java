package ru.spbau.task2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class HashMapTrieNode {
    private final Map<Character, HashMapTrieNode> nextNodes = new HashMap<>();
    private boolean isTerminal = false;
    private int size = 0;

    public boolean add(String element, int fromPosition) {
        if (fromPosition >= element.length()) {
            return addWordToCurrentNode();
        }

        char currentChar = element.charAt(fromPosition);
        HashMapTrieNode nextNode = getOrCreateNextNode(currentChar);
        boolean added = nextNode.add(element, fromPosition + 1);
        if (added) {
            size++;
        }
        return added;
    }

    public boolean contains(String element, int fromPosition) {
        if (fromPosition >= element.length()) {
            return isTerminal;
        }

        char currentChar = element.charAt(fromPosition);
        return getNextNode(currentChar)
            .map(node -> node.contains(element, fromPosition + 1))
            .orElse(false);
    }

    public boolean remove(String element, int fromPosition) {
        if (fromPosition >= element.length()) {
            return removeWordFromCurrentNode();
        }

        char currentChar = element.charAt(fromPosition);
        boolean isDeleted = getNextNode(currentChar)
            .map(node -> node.remove(element, fromPosition + 1))
            .orElse(false);

        if (isDeleted) {
            removeNextNodeIfEmpty(currentChar);
            size--;
        }

        return isDeleted;
    }

    public int size() {
        return size;
    }

    public int countByPrefix(String prefix, int fromPosition) {
        if (fromPosition >= prefix.length()) {
            return size;
        }

        char currentChar = prefix.charAt(fromPosition);
        return getNextNode(currentChar)
            .map(node -> node.countByPrefix(prefix, fromPosition + 1))
            .orElse(0);
    }

    private boolean addWordToCurrentNode() {
        if (isTerminal) {
            return false;
        } else {
            isTerminal = true;
            size++;
            return true;
        }
    }

    private boolean removeWordFromCurrentNode() {
        if (isTerminal) {
            isTerminal = false;
            size--;
            return true;
        } else {
            return false;
        }
    }

    private void removeNextNodeIfEmpty(char currentChar) {
        if (nextNodes.get(currentChar).size() == 0) {
            nextNodes.remove(currentChar);
        }
    }

    private Optional<HashMapTrieNode> getNextNode(char currentChar) {
        return Optional.ofNullable(nextNodes.get(currentChar));
    }

    private HashMapTrieNode getOrCreateNextNode(char currentChar) {
        return nextNodes.computeIfAbsent(currentChar, c -> new HashMapTrieNode());
    }
}
