package ru.hse.spb.git;

import lombok.Data;

import java.util.Optional;

@Data
public class Commit {
    private final String hash;
    private final String treeHash;
    private final String message;
    private final String parentHash;

    public Optional<String> getParentHash() {
        return Optional.ofNullable(parentHash);
    }
}
