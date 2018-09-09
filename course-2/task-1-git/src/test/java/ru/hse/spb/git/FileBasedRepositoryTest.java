package ru.hse.spb.git;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class FileBasedRepositoryTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test(expected = IllegalArgumentException.class)
    public void init_fails_if_directory_does_not_exists() throws IOException {
        Path notExistingDir = Paths.get("not_existing_dir");

        assertThat(notExistingDir).doesNotExist();

        FileBasedRepository.init(notExistingDir);
    }

    @Test(expected = IllegalArgumentException.class)
    public void init_fails_if_file_is_passed_instad_of_directory() throws IOException {
        Path notDirectory = temporaryFolder.newFile("not_directory").toPath();

        assertThat(notDirectory).exists();

        FileBasedRepository.init(notDirectory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void init_fails_if_there_is_file_with_name_mygit() throws IOException {
        File myGitFile = temporaryFolder.newFile(".mygit");

        assertThat(myGitFile).exists();

        FileBasedRepository.init(temporaryFolder.getRoot().toPath());
    }

    @Test
    public void init_creates_metadata_directory_when_called_on_empty_folder() throws IOException {
        Path rootDirectory = temporaryFolder.getRoot().toPath();
        Path myGitDir = rootDirectory.resolve(".mygit");

        assertThat(myGitDir).doesNotExist();

        FileBasedRepository repository = FileBasedRepository.init(rootDirectory);

        assertThat(myGitDir).isDirectory();
        assertThat(myGitDir.resolve("objects")).isDirectory();
        assertThat(myGitDir.resolve("HEAD")).isRegularFile();
        assertThat(repository.getRootDirectory()).isSameAs(rootDirectory);
    }
}