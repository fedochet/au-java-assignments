package ru.hse.spb.git.status;

import lombok.Data;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Data
public final class StatusBuilder implements Status {
    private final Set<Path> notTrackedFiles = new HashSet<>();
    private final Set<Path> committedFiles = new HashSet<>();
    private final Set<Path> stagedFiles = new HashSet<>();
    private final Set<Path> notStagedFiles = new HashSet<>();
    private final Set<Path> deletedFiles = new HashSet<>();
    private final Set<Path> missingFiles = new HashSet<>();

    public StatusBuilder withCommittedFiles(Path... paths) {
        Collections.addAll(committedFiles, paths);
        return this;
    }

    public StatusBuilder withStagedFiles(Path... paths) {
        Collections.addAll(stagedFiles, paths);
        return this;
    }

    public StatusBuilder withNotStagedFiles(Path... paths) {
        Collections.addAll(notStagedFiles, paths);
        return this;
    }

    public StatusBuilder withDeletedFiles(Path... paths) {
        Collections.addAll(deletedFiles, paths);
        return this;
    }

    public StatusBuilder withNotTrackedFiles(Path... paths) {
        Collections.addAll(notTrackedFiles, paths);
        return this;
    }

    public StatusBuilder withMissingFiles(Path... paths) {
        Collections.addAll(missingFiles, paths);
        return this;
    }
}
