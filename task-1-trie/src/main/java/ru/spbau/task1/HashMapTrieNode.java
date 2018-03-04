package ru.spbau.task1;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class HashMapTrieNode implements TrieNode {
    private Map<Character, TrieNode> nextNodes = new HashMap<>();
    private boolean isTerminal = false;
    private int size = 0;

    @Override
    public boolean add(String element, int fromPosition) {
        if (fromPosition >= element.length()) {
            return addWordToCurrentNode();
        }

        char currentChar = element.charAt(fromPosition);
        TrieNode nextNode = getOrCreateNextNode(currentChar);
        boolean added = nextNode.add(element, fromPosition + 1);
        if (added) {
            size++;
        }
        return added;
    }

    @Override
    public boolean contains(String element, int fromPosition) {
        if (fromPosition >= element.length()) {
            return isTerminal;
        }

        char currentChar = element.charAt(fromPosition);
        return getNextNode(currentChar)
            .map(node -> node.contains(element, fromPosition + 1))
            .orElse(false);
    }

    @Override
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

    @Override
    public int size() {
        return size;
    }

    @Override
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

    private Optional<TrieNode> getNextNode(char currentChar) {
        return Optional.ofNullable(nextNodes.get(currentChar));
    }

    private TrieNode getOrCreateNextNode(char currentChar) {
        return nextNodes.computeIfAbsent(currentChar, c -> new HashMapTrieNode());
    }
}
