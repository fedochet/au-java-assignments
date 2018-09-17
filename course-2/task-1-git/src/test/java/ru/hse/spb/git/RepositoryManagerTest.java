package ru.hse.spb.git;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
        repository = new RepositoryManager(tempFolder.getRoot().toPath());
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

        RepositoryManager alternativeRepository = new RepositoryManager(tempFolder.getRoot().toPath());

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
    @Ignore("Currently this does not work because of no index")
    public void files_with_same_content_are_not_comitted_at_once() throws IOException {
        Path newFileOne = createFile("new_file_1", "");
        Path newFileTwo = createFile("new_file_2", "");

        String hash = repository.commitFile(newFileOne, "first commit");
        Files.delete(newFileOne);
        Files.delete(newFileTwo);
        repository.checkoutTo(hash);

        assertThat(newFileTwo).doesNotExist();
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