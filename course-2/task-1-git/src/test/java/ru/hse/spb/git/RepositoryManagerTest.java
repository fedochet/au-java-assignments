package ru.hse.spb.git;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import ru.hse.spb.git.commit.CommitInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryManagerTest extends TempDirectoryTestBase {

    private RepositoryManager repository;

    @Before
    public void initRepository() throws IOException {
        repository = RepositoryManager.init(tempFolder.getRoot().toPath());
    }

    @Test
    public void after_init_log_is_empty() throws IOException {
        assertThat(repository.getLog()).isEmpty();
    }

    @Test
    public void head_of_empty_repository_does_not_exisit() throws IOException {
        assertThat(repository.getHeadCommit()).isEmpty();
    }

    @Test
    public void manager_can_use_already_initialized_dir() throws IOException {
        Path newFile = createFile("new_file", "");
        repository.addFile(newFile);
        repository.commit("first commit");

        RepositoryManager alternativeRepository = RepositoryManager.open(tempFolder.getRoot().toPath()).get();

        assertThat(alternativeRepository.getHeadCommit()).isEqualTo(repository.getHeadCommit());
    }

    @Test
    public void new_commit_becomes_head() throws IOException {
        Path newFile = createFile("new_file", "");

        repository.addFile(newFile);
        String hash = repository.commit("first commit");

        assertThat(repository.getHeadCommit()).contains(hash);
    }

    @Test
    public void new_commit_can_be_seen_in_log() throws IOException {
        Path newFileOne = createFile("new_file_1", "file 1");
        Path newFileTwo = createFile("new_file_2", "file 2");

        repository.addFile(newFileOne);
        String commitOne = repository.commit("first commit");
        repository.addFile(newFileTwo);
        String commitTwo = repository.commit("second commit");

        assertThat(repository.getLog()).containsExactly(
            new CommitInfo(commitTwo, Instant.now(), "second commit"),
            new CommitInfo(commitOne, Instant.now(), "first commit")
        );
    }

    @Test
    public void file_can_be_committed_and_then_restored() throws IOException {
        String newFileContent = "Hello world";
        Path newFile = createFile("new_file", newFileContent);

        String hash = repository.commitFile(newFile, "first commit");
        Files.delete(newFile);
        repository.checkoutToCommit(hash);

        assertThat(newFile).hasContent(newFileContent);
    }

    @Test
    public void not_committed_files_are_not_restored() throws IOException {
        Path newFileOne = createFile("new_file_1", "file 1");
        Path newFileTwo = createFile("new_file_2", "file 2");

        String hash = repository.commitFile(newFileOne, "first commit");
        Files.delete(newFileOne);
        Files.delete(newFileTwo);
        repository.checkoutToCommit(hash);

        assertThat(newFileTwo).doesNotExist();
    }

    @Test
    public void file_in_directory_can_be_committed() throws IOException {
        String fileContent = "file 1";
        Path newFile = createFile(Paths.get("dir", "new_file"), fileContent);

        String hash = repository.commitFile(newFile, "first commit");
        FileUtils.deleteDirectory(newFile.getParent().toFile());
        repository.checkoutToCommit(hash);

        assertThat(newFile).hasContent(fileContent);
    }

    @Test
    public void files_with_same_content_are_not_committed_at_once() throws IOException {
        Path newFileOne = createFile("new_file_1", "");
        Path newFileTwo = createFile("new_file_2", "");

        String hash = repository.commitFile(newFileOne, "first commit");
        Files.delete(newFileOne);
        Files.delete(newFileTwo);
        repository.checkoutToCommit(hash);

        assertThat(newFileTwo).doesNotExist();
    }

    @Test
    public void files_with_same_content_can_be_comitted_in_different_commits() throws IOException {
        Path newFileOne = createFile("new_file_1", "123");
        Path newFileTwo = createFile("new_file_2", "123");

        String hashOne = repository.commitFile(newFileOne, "first commit");
        String hashTwo = repository.commitFile(newFileTwo, "second commit");

        assertThat(hashOne).isNotEqualTo(hashTwo);
        assertThat(repository.getHeadCommit()).contains(hashTwo);
    }

    @Test
    public void files_with_same_content_can_be_checkouted() throws IOException {
        Path newFileOne = createFile("new_file_1", "123");
        Path newFileTwo = createFile("new_file_2", "123");

        String hashOne = repository.commitFile(newFileOne, "first commit");
        String hashTwo = repository.commitFile(newFileTwo, "second commit");

        repository.checkoutToCommit(hashOne);

        assertThat(newFileOne).hasContent("123");
        assertThat(newFileTwo).doesNotExist();

        repository.checkoutToCommit(hashTwo);

        assertThat(newFileOne).hasContent("123");
        assertThat(newFileTwo).hasContent("123");
    }

    @Test
    public void files_in_two_directories_can_be_committed() throws IOException {
        Path newFileOne = createFile(Paths.get("dir1", "new_file_1"), "file1");
        Path newFileTwo = createFile(Paths.get("dir2", "new_file_2"), "file2");

        String hashOne = repository.commitFile(newFileOne, "commit 1");
        String hashTwo = repository.commitFile(newFileTwo, "commit 2");

        repository.checkoutToCommit(hashOne);
        assertThat(newFileOne).hasContent("file1");
        assertThat(newFileTwo.getParent()).doesNotExist();

        repository.checkoutToCommit(hashTwo);
        assertThat(newFileOne).hasContent("file1");
        assertThat(newFileTwo).hasContent("file2");

    }

    @Test
    public void after_reset_heads_are_reset_and_files_are_removed() throws IOException {
        Path newFileOne = createFile("new_file_1", "file1");
        Path newFileTwo = createFile("new_file_2", "file2");

        String hashOne = repository.commitFile(newFileOne, "commit 1");
        repository.commitFile(newFileTwo, "commit 2");

        repository.hardResetTo(hashOne);

        assertThat(repository.getHeadCommit()).contains(hashOne);
        assertThat(newFileOne).hasContent("file1");
        assertThat(newFileTwo).doesNotExist();
    }

    @Test
    public void after_checkout_head_is_set_to_commit_hash() throws IOException {
        Path newFileOne = createFile("new_file_1", "file1");
        Path newFileTwo = createFile("new_file_2", "file2");

        String hashOne = repository.commitFile(newFileOne, "commit 1");
        repository.commitFile(newFileTwo, "commit 2");

        repository.checkoutToCommit(hashOne);

        assertThat(repository.getHeadCommit()).contains(hashOne);
    }

    @Test
    public void after_checkout_master_head_is_not_changed() throws IOException {
        Path newFileOne = createFile("new_file_1", "file1");
        Path newFileTwo = createFile("new_file_2", "file2");

        String hashOne = repository.commitFile(newFileOne, "commit 1");
        String hashTwo = repository.commitFile(newFileTwo, "commit 2");

        repository.checkoutToCommit(hashOne);
    }

    @Test
    public void checkout_file_do_nothing_if_file_is_unchanged() throws IOException {
        Path newFile = createFile("new_file", "file1");
        repository.addFile(newFile);
        repository.commit("commit 1");

        repository.checkoutFile(newFile);

        assertThat(newFile).hasContent("file1");
    }

    @Test
    public void checkout_file_restores_committed_file_if_file_is_changed() throws IOException {
        Path newFile = createFile("new_file", "file1");
        repository.addFile(newFile);
        repository.commit("commit 1");
        FileUtils.write(newFile.toFile(), "file1 changed", StandardCharsets.UTF_8);

        repository.checkoutFile(newFile);

        assertThat(newFile).hasContent("file1");
    }

    @Test
    public void checkout_file_restores_committed_file_if_file_is_deleted() throws IOException {
        Path newFile = createFile("new_file", "file1");
        repository.addFile(newFile);
        repository.commit("commit 1");
        Files.delete(newFile);

        repository.checkoutFile(newFile);

        assertThat(newFile).hasContent("file1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkouting_not_committed_file_throws_exception() throws IOException {
        repository.checkoutFile(createFile("file_1", "file1"));
    }

    @Test
    public void checkouting_file_restores_staged_file_if_file_is_changed() throws IOException {
        Path newFile = createFile("new_file", "file1");
        repository.addFile(newFile);
        FileUtils.write(newFile.toFile(), "file1 changed", StandardCharsets.UTF_8);

        repository.checkoutFile(newFile);

        assertThat(newFile).hasContent("file1");
    }

    @Test
    public void checkouting_file_restores_staged_file_if_file_is_deleted() throws IOException {
        Path newFile = createFile("new_file", "file1");
        repository.addFile(newFile);
        Files.delete(newFile);

        repository.checkoutFile(newFile);

        assertThat(newFile).hasContent("file1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleted_file_cannot_be_checkouted() throws IOException {
        Path newFile = createFile("new_file", "file1");
        repository.addFile(newFile);
        repository.commit("commit 1");
        repository.remove(newFile);

        repository.checkoutFile(newFile);
    }

    @Test
    public void head_is_pointing_to_master_after_checkouting_to_master_head() throws IOException {
        Path newFileOne = createFile("new_file_1", "file1");
        Path newFileTwo = createFile("new_file_2", "file2");
        Path newFileThree = createFile("new_file_3", "file3");

        repository.addFile(newFileOne);
        String hashOne = repository.commit("commit 1");
        repository.addFile(newFileTwo);
        String hashTwo = repository.commit("commit 2");

        repository.checkoutToCommit(hashOne);
        repository.checkoutToCommit(hashTwo);

        repository.addFile(newFileThree);
        String hashThree = repository.commit("commit 3");

        assertThat(repository.getHeadCommit()).contains(hashThree);
        assertThat(repository.getLog()).hasSize(3);
    }

}