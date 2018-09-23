package ru.hse.spb.git;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
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
    public void manager_can_use_already_initialized_dir() throws IOException {
        Path newFile = createFile("new_file", "");
        repository.commitFile(newFile, "first commit");

        RepositoryManager alternativeRepository = RepositoryManager.open(tempFolder.getRoot().toPath()).get();

        assertThat(alternativeRepository.getHeadCommit()).isEqualTo(repository.getHeadCommit());
    }

    @Test
    public void new_commit_becomes_head() throws IOException {
        Path newFile = createFile("new_file", "");

        String hash = repository.commitFile(newFile, "first commit");

        assertThat(repository.getHeadCommit()).contains(hash);
    }

    @Test
    public void new_commit_can_be_seen_in_log() throws IOException {
        Path newFileOne = createFile("new_file_1", "file 1");
        Path newFileTwo = createFile("new_file_2", "file 2");

        String commitOne = repository.commitFile(newFileOne, "first commit");
        String commitTwo = repository.commitFile(newFileTwo, "second commit");

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
        repository.checkoutTo(hash);

        assertThat(newFile).hasContent(newFileContent);
    }

    @Test
    public void not_committed_files_are_not_restored() throws IOException {
        Path newFileOne = createFile("new_file_1", "file 1");
        Path newFileTwo = createFile("new_file_2", "file 2");

        String hash = repository.commitFile(newFileOne, "first commit");
        Files.delete(newFileOne);
        Files.delete(newFileTwo);
        repository.checkoutTo(hash);

        assertThat(newFileTwo).doesNotExist();
    }

    @Test
    public void file_in_directory_can_be_committed() throws IOException {
        String fileContent = "file 1";
        Path newFile = createFile(Paths.get("dir", "new_file"), fileContent);

        String hash = repository.commitFile(newFile, "first commit");
        FileUtils.deleteDirectory(newFile.getParent().toFile());
        repository.checkoutTo(hash);

        assertThat(newFile).hasContent(fileContent);
    }

    @Test
    public void files_with_same_content_are_not_committed_at_once() throws IOException {
        Path newFileOne = createFile("new_file_1", "");
        Path newFileTwo = createFile("new_file_2", "");

        String hash = repository.commitFile(newFileOne, "first commit");
        Files.delete(newFileOne);
        Files.delete(newFileTwo);
        repository.checkoutTo(hash);

        assertThat(newFileTwo).doesNotExist();
    }

    @Test
    public void files_in_two_directories_can_be_committed() throws IOException {
        Path newFileOne = createFile(Paths.get("dir1", "new_file_1"), "file1");
        Path newFileTwo = createFile(Paths.get("dir2", "new_file_2"), "file2");

        String hashOne = repository.commitFile(newFileOne, "commit 1");
        String hashTwo = repository.commitFile(newFileTwo, "commit 2");

        repository.checkoutTo(hashOne);
        assertThat(newFileOne).hasContent("file1");
        assertThat(newFileTwo.getParent()).doesNotExist();

        repository.checkoutTo(hashTwo);
        assertThat(newFileOne).hasContent("file1");
        assertThat(newFileTwo).hasContent("file2");

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