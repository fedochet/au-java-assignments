package ru.hse.spb.git;

import lombok.Data;

@Data
public class CommitInfo {
    private final String hash;
    private final String message;
}
