package ru.hse.spb.git;

import lombok.Data;

@Data
public class Commit {
    private final String hash;
    private final String treeHash;
    private final String message;
}
