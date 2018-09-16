package ru.hse.spb.git;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public void head_of_empty_repository_does_not_exisit() throws IOException {
        assertThat(repository.getHeadCommit()).isEmpty();
    }

    @Test
    public void new_commit_becomes_head() throws IOException {
        Path newFile = createFile("new_file", "");

        String hash = repository.commitFile(newFile, "first commit");

        assertThat(repository.getHeadCommit()).contains(hash);
    }

    @Test
    public void file_can_be_commited_and_then_restored() throws IOException {
        String newFileContent = "Hello world";
        Path newFile = createFile("new_file", newFileContent);

        String hash = repository.commitFile(newFile, "first commit");
        Files.delete(newFile);
        repository.resetTo(hash);

        assertThat(newFile).hasContent(newFileContent);
    }

    private Path createFile(String name, String content) throws IOException {
        Path file = tempFolder.newFile(name).toPath();
        IOUtils.write(content, Files.newOutputStream(file), "UTF-8");

        return file;
    }
}