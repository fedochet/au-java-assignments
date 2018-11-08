package ru.hse.spb.git.status;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Set;

public interface Status {

    /**
     * Current branch, can be null if repository in deattached state.
     * Should be 'master' on start.
     */
    @Nullable
    String getBranch();

    /**
     * Current commit, can be null if repository is just initialized.
     */
    @Nullable
    String getCommit();

    /**
     * New files.
     */
    @NotNull
    Set<Path> getNotTrackedFiles();

    /**
     * Modified but not yet staged files.
     */
    @NotNull
    Set<Path> getNotStagedFiles();

    /**
     * Files staged for the next commit.
     */
    @NotNull
    Set<Path> getStagedFiles();

    /**
     * Files contained in current commit.
     */
    @NotNull
    Set<Path> getCommittedFiles();

    /**
     * Files staged for deletion.
     */
    @NotNull
    Set<Path> getDeletedFiles();

    /**
     * Committed but missing files.
     */
    @NotNull
    Set<Path> getMissingFiles();
}
