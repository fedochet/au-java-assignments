package ru.spbau.task1;

import java.util.HashMap;
import java.util.Map;

class HashMapTrieNode implements TrieNode {
    private Map<Character, TrieNode> nextNodes = new HashMap<>();
    private boolean isTerminal = false;
    private int size = 0;

    @Override
    public boolean add(String element, int fromPosition) {
        if (fromPosition >= element.length()) {
            if (isTerminal) {
                return false;
            } else {
                isTerminal = true;
                size++;
                return true;
            }
        }

        char currentChar = element.charAt(fromPosition);
        boolean newNodesCreated = !nextNodes.containsKey(currentChar);
        TrieNode nextNode = nextNodes.computeIfAbsent(currentChar, c -> new HashMapTrieNode());
        boolean addedInNextNodes = nextNode.add(element, fromPosition + 1);
        boolean isAdded = newNodesCreated || addedInNextNodes;
        if (isAdded) {
            size++;
        }
        return isAdded;

    }

    @Override
    public boolean contains(String element, int fromPosition) {
        if (fromPosition >= element.length()) {
            return isTerminal;
        }

        char currentChar = element.charAt(fromPosition);
        return nextNodes.containsKey(currentChar)
            && nextNodes.get(currentChar).contains(element, fromPosition + 1);
    }

    @Override
    public boolean remove(String element, int fromPosition) {
        if (fromPosition >= element.length()) {
            if (isTerminal) {
                isTerminal = false;
                size--;
                return true;
            } else {
                return false;
            }
        }

        char currentChar = element.charAt(fromPosition);
        boolean isDeleted = nextNodes.containsKey(currentChar)
            && nextNodes.get(currentChar).remove(element, fromPosition + 1);

        if (isDeleted) {
            size--;
            if (nextNodes.get(currentChar).size() == 0) {
                nextNodes.remove(currentChar);
            }
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
        if (nextNodes.containsKey(currentChar)) {
            return nextNodes.get(currentChar).countByPrefix(prefix, fromPosition + 1);
        } else {
            return 0;
        }
    }
}
