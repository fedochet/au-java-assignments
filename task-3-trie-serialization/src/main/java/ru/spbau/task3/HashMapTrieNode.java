package ru.spbau.task3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class HashMapTrieNode implements StreamSerializable {
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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        HashMapTrieNode that = (HashMapTrieNode) obj;
        return (size == that.size)
            && (isTerminal == that.isTerminal)
            && nextNodes.equals(that.nextNodes);
    }

    @Override
    public void serialize(@NotNull OutputStream out) throws IOException {
        DataOutputStream dataOutputStream = getDataOutputStream(out);
        dataOutputStream.writeInt(size);
        dataOutputStream.writeBoolean(isTerminal);

        serializeNextNodes(dataOutputStream, nextNodes);
    }

    @Override
    public void deserialize(@NotNull InputStream in) throws IOException {
        DataInputStream dataOutputStream = getDataInputStream(in);
        int newSize = dataOutputStream.readInt();
        boolean newIsTerminal = dataOutputStream.readBoolean();

        Map<Character, HashMapTrieNode> newNextNodes = deserializeNextNodes(dataOutputStream);

        nextNodes.clear();
        nextNodes.putAll(newNextNodes);
        isTerminal = newIsTerminal;
        size = newSize;
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

    private DataOutputStream getDataOutputStream(OutputStream out) {
        if (out instanceof DataOutputStream) {
            return (DataOutputStream) out;
        }

        return new DataOutputStream(out);
    }

    private DataInputStream getDataInputStream(InputStream in) {
        if (in instanceof DataInputStream) {
            return (DataInputStream) in;
        }

        return new DataInputStream(in);
    }

    private void serializeNextNodes(DataOutputStream dataOutputStream,
                                    Map<Character, HashMapTrieNode> map) throws IOException {
        dataOutputStream.writeInt(map.size());
        for (Map.Entry<Character, HashMapTrieNode> entry : this.nextNodes.entrySet()) {
            dataOutputStream.writeChar(entry.getKey());
            entry.getValue().serialize(dataOutputStream);
        }
    }

    private Map<Character, HashMapTrieNode> deserializeNextNodes(DataInputStream dataOutputStream) throws IOException {
        int dictSize = dataOutputStream.readInt();
        Map<Character, HashMapTrieNode> map = new HashMap<>();

        for (int i = 0; i < dictSize; i++) {
            char key = dataOutputStream.readChar();
            HashMapTrieNode value = new HashMapTrieNode();
            value.deserialize(dataOutputStream);
            map.put(key, value);
        }

        return map;
    }
}
