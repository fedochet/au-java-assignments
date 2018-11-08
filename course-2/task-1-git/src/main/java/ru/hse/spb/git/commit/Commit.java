package ru.hse.spb.git.commit;

import lombok.Data;

import java.time.Instant;
import java.util.Optional;

@Data
public class Commit {
    private final String hash;
    private final String treeHash;
    private final String message;
    private final String parentHash;
    private final Instant timestamp;

    public Optional<String> getParentHash() {
        return Optional.ofNullable(parentHash);
    }
}
