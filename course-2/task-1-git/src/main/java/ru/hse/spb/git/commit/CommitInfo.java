package ru.hse.spb.git.commit;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
public class CommitInfo {
    private final String hash;
    @EqualsAndHashCode.Exclude
    private final Instant timestamp;
    private final String message;
}
