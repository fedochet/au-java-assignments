package ru.hse.spb.git;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import ru.hse.spb.git.status.StatusBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryManagerStatusTest extends TempDirectoryTestBase {
    private RepositoryManager repository;

    @Before
    public void initRepository() throws IOException {
        repository = RepositoryManager.init(tempFolder.getRoot().toPath());
    }

    @Test
    public void status_of_empty_repository_is_empty() throws IOException {
        assertThat(repository.getStatus()).isEqualTo(onMaster());
    }

    @Test
    public void not_added_file_is_in_not_tracked_files() throws IOException {
        Path newFileOne = createFile("new_file_1", "file1");
        Path newFileTwo = createFile(Paths.get("dir1", "new_file_2"), "file2");
        Path newFileThree = createFile(Paths.get("dir2", "dir3", "new_file_3"), "file3");

        assertThat(repository.getStatus()).isEqualTo(
            onMaster()
                .withNotTrackedFiles(newFileOne)
                .withNotTrackedFiles(newFileTwo)
                .withNotTrackedFiles(newFileThree)
        );
    }

    @Test
    public void after_addition_file_can_be_seen_in_added_files() throws IOException {
        Path newFileOne = createFile("new_file_1", "");

        repository.addFile(newFileOne);
        assertThat(repository.getStatus()).isEqualTo(
            onMaster().withStagedFiles(newFileOne)
        );

        Path newFileTwo = createFile("new_file_2", "");

        repository.addFile(newFileTwo);
        assertThat(repository.getStatus()).isEqualTo(
            onMaster()
                .withStagedFiles(newFileOne)
                .withStagedFiles(newFileTwo)
        );
    }

    @Test
    public void after_commit_file_can_be_seen_in_committed_files() throws IOException {
        Path newFile = createFile("new_file", "");

        repository.addFile(newFile);
        String commit1 = repository.commit("first commit");

        assertThat(repository.getStatus()).isEqualTo(
            onMasterCommit(commit1).withCommittedFiles(newFile)
        );
    }

    @Test
    public void committed_but_changed_file_can_be_seen_in_not_staged_files() throws IOException {
        Path newFile = createFile("new_file", "");
        repository.addFile(newFile);
        String commit1 = repository.commit("first commit");

        FileUtils.write(newFile.toFile(), "content", UTF_8);

        assertThat(repository.getStatus()).isEqualTo(
            onMasterCommit(commit1).withNotStagedFiles(newFile)
        );
    }

    @Test
    public void committed_but_deleted_file_can_be_seen_in_missing_files() throws IOException {
        Path newFile = createFile("new_file", "");
        repository.addFile(newFile);
        String commit1 = repository.commit("first commit");

        Files.delete(newFile);

        assertThat(repository.getStatus()).isEqualTo(
            onMasterCommit(commit1).withMissingFiles(newFile)
        );
    }

    @Test
    public void removed_with_repository_files_can_be_seen_in_deleted_files() throws IOException {
        Path newFile = createFile("new_file", "");
        repository.addFile(newFile);
        String commit1 = repository.commit("first commit");

        repository.remove(newFile);

        assertThat(repository.getStatus()).isEqualTo(
            onMasterCommit(commit1).withDeletedFiles(newFile)
        );
    }

    @Test
    public void deleted_and_then_removed_with_repository_file_is_in_deleted_files() throws IOException {
        Path newFile = createFile("new_file", "");
        repository.addFile(newFile);
        String commit1 = repository.commit("first commit");

        Files.delete(newFile);
        repository.remove(newFile);

        assertThat(repository.getStatus()).isEqualTo(
            onMasterCommit(commit1).withDeletedFiles(newFile)
        );
    }

    @NotNull
    private StatusBuilder onMasterCommit(String commit1) {
        return onMaster().onCommit(commit1);
    }

    @NotNull
    private StatusBuilder onMaster() {
        return new StatusBuilder().onBranch("master");
    }

}