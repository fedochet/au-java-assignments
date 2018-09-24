package ru.hse.spb.git;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.hse.spb.git.commit.CommitInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;

public class RepositoryManagerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
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
    public void status_of_empty_repository_is_empty() throws IOException {
        assertThat(repository.getStatus()).isEqualTo(new StatusBuilder());
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
    public void not_added_file_is_in_not_tracked_files() throws IOException {
        Path newFileOne = createFile("new_file_1", "file1");
        Path newFileTwo = createFile(Paths.get("dir1", "new_file_2"), "file2");
        Path newFileThree = createFile(Paths.get("dir2", "dir3", "new_file_3"), "file3");

        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withNotTrackedFile(newFileOne)
                .withNotTrackedFile(newFileTwo)
                .withNotTrackedFile(newFileThree)
        );
    }

    @Test
    public void after_addition_file_can_be_seen_in_added_files() throws IOException {
        Path newFileOne = createFile("new_file_1", "");

        repository.addFile(newFileOne);
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder().withAddedFile(newFileOne)
        );

        Path newFileTwo = createFile("new_file_2", "");

        repository.addFile(newFileTwo);
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withAddedFile(newFileOne)
                .withAddedFile(newFileTwo)
        );
    }

    @Test
    public void new_commit_becomes_head() throws IOException {
        Path newFile = createFile("new_file", "");

        repository.addFile(newFile);
        String hash = repository.commit("first commit");

        assertThat(repository.getHeadCommit()).contains(hash);
    }

    @Test
    public void after_commit_file_can_be_seen_in_committed_files() throws IOException {
        Path newFile = createFile("new_file", "");

        repository.addFile(newFile);
        repository.commit("first commit");

        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder().withCommittedFile(newFile)
        );
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
            new CommitInfo(commitTwo, "second commit"),
            new CommitInfo(commitOne, "first commit")
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
        assertThat(repository.getMasterHeadCommit()).contains(hashTwo);
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
    public void after_reset_head_is_set_to_commit_hash() throws IOException {
        Path newFileOne = createFile("new_file_1", "file1");
        Path newFileTwo = createFile("new_file_2", "file2");

        String hashOne = repository.commitFile(newFileOne, "commit 1");
        repository.commitFile(newFileTwo, "commit 2");

        repository.resetTo(hashOne);
        assertThat(repository.getHeadCommit()).contains(hashOne);
    }

    @Test
    public void after_reset_master_head_is_changed() throws IOException {
        Path newFileOne = createFile("new_file_1", "file1");
        Path newFileTwo = createFile("new_file_2", "file2");

        String hashOne = repository.commitFile(newFileOne, "commit 1");
        repository.commitFile(newFileTwo, "commit 2");

        repository.resetTo(hashOne);

        assertThat(repository.getMasterHeadCommit()).contains(hashOne);
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

        assertThat(repository.getMasterHeadCommit()).contains(hashTwo);
    }

    // This test somehow verifies that index is cleared correctly on checkouts
    @Test
    public void complex_workflow_test() throws IOException {
        Path newFileOne = createFile("new_file_1", "file1");
        Path newFileTwo = createFile("new_file_2", "file2");

        String hashOne = repository.commitFile(newFileOne, "commit 1");
        String hashTwo = repository.commitFile(newFileTwo, "commit 2");

        repository.resetTo(hashOne);

        assertThat(newFileOne).hasContent("file1");
        assertThat(newFileTwo).doesNotExist();
        assertThat(repository.getHeadCommit()).contains(hashOne);
        assertThat(repository.getMasterHeadCommit()).contains(hashOne);

        newFileTwo = createFile("new_file_2", "file2");
        Path newFileThree = createFile("new_file_3", "file3");

        String hashThree = repository.commitFile(newFileOne, "commit 3");

        assertThat(newFileOne).hasContent("file1");
        assertThat(newFileThree).hasContent("file3");
        assertThat(repository.getHeadCommit()).contains(hashThree);
        assertThat(repository.getMasterHeadCommit()).contains(hashThree);

        Files.delete(newFileTwo);
        repository.checkoutToCommit(hashOne);
        repository.checkoutToCommit(hashThree);

        assertThat(newFileOne).hasContent("file1");
        assertThat(newFileTwo).doesNotExist();
        assertThat(newFileThree).hasContent("file3");
        assertThat(repository.getHeadCommit()).contains(hashThree);
        assertThat(repository.getMasterHeadCommit()).contains(hashThree);
    }

    private Path createFile(Path file, String content) throws IOException {
        Path newFile = tempFolder.getRoot().toPath().resolve(file);
        Files.createDirectories(newFile.getParent());
        IOUtils.write(content, Files.newOutputStream(newFile), "UTF-8");

        return newFile;
    }

    private Path createFile(String name, String content) throws IOException {
        Path file = tempFolder.newFile(name).toPath();
        FileUtils.write(file.toFile(), content, "UTF-8");

        return file;
    }
}