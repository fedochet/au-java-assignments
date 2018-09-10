package ru.hse.spb.git;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;

public class FileBasedRepositoryOperationsTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();
    private Repository repository;

    @Before
    public void initRepository() throws IOException {
        repository = FileBasedRepository.init(temporaryFolder.getRoot().toPath());
    }

    @Test
    public void head_is_empty_after_init() {
        assertThat(repository.getHead()).isEmpty();
    }

    @Test
    public void blob_can_be_created_from_file() throws IOException {
        Path file = temporaryFolder.newFile("file.txt").toPath();

        Blob blob = repository.createBlob(file);

        assertThat(blob.getFile()).isRegularFile();
        assertThat(blob.getFile()).hasFileName(blob.getHash());
        assertThat(blob.getHash()).isEqualTo(repository.hashBlob(file));
    }

    @Test
    public void content_of_original_file_can_be_read_from_blob() throws IOException {
        Path file = temporaryFolder.newFile("file.txt").toPath();
        Files.write(file, "some text".getBytes());

        Blob blob = repository.createBlob(file);

        assertArrayEquals(Files.readAllBytes(file), repository.getBlobContent(blob));
    }

    @Test(expected = IllegalArgumentException.class)
    public void blob_cannot_be_created_if_it_already_exists() throws IOException {
        Path file = temporaryFolder.newFile("file.txt").toPath();

        repository.createBlob(file);
        repository.createBlob(file);
    }

    @Test
    public void blob_can_be_found_by_its_hash() throws IOException {
        Path file = temporaryFolder.newFile("file.txt").toPath();

        Blob blob = repository.createBlob(file);

        assertThat(repository.getBlob(blob.getHash())).contains(blob);
    }

    @Test
    public void blob_cannot_be_found_if_its_not_created() throws IOException {
        Path file = temporaryFolder.newFile("file.txt").toPath();

        assertThat(repository.getBlob(repository.hashBlob(file))).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void blob_content_cannot_be_read_if_no_marker_available() throws IOException {
        Path file = temporaryFolder.newFile("file.txt").toPath();
        Files.write(file, "some text".getBytes());

        Blob blob = repository.createBlob(file);
        Files.write(blob.getFile(), "some text".getBytes());

        repository.getBlobContent(blob);
    }
}