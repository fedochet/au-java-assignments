package org.anstreth.torrent.client.storage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import static org.assertj.core.api.Assertions.*;

public class FilePartsRepositoryImplTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private FilePartsRepository repository;

    @Before
    public void setUp() throws IOException {
        repository = new FilePartsRepositoryImpl(folder.newFolder(".meta").toPath());
    }

    @Test
    public void at_start_repository_is_empty() throws IOException {
        assertThat(repository.listFiles()).isEmpty();
    }

    @Test
    public void non_existing_file_cannot_be_found() {
        assertThatThrownBy(() -> repository.getFile(0)).isInstanceOf(NoSuchFileException.class);
    }

    @Test
    public void file_can_be_added_and_then_looked_up() throws IOException {
        repository.addFileWithoutParts(0, 10);

        FilePartsDetails file = repository.getFile(0);

        assertThat(file.getFileId()).isEqualTo(0);
        assertThat(file.getNumberOfParts()).isEqualTo(10);
        assertThat(file.getReadyParts()).isEmpty();
    }

    @Test
    public void file_can_be_added_with_all_parts() throws IOException {
        repository.addFileWithAllParts(0, 3);

        assertThat(repository.getFile(0).getReadyParts())
            .containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    public void file_cannot_be_created_with_less_than_one_part() {
        assertThatThrownBy(() -> repository.addFileWithoutParts(123, -1))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> repository.addFileWithoutParts(123, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void after_saving_part_it_appears_in_readyParts() throws IOException {
        repository.addFileWithoutParts(0, 10);

        repository.savePart(0, 0);
        repository.savePart(0, 3);
        repository.savePart(0, 5);

        assertThat(repository.getFile(0).getReadyParts())
            .containsExactlyInAnyOrder(0, 3, 5);
    }

    @Test
    public void saving_same_part_twice_results_in_exception() throws IOException {
        repository.addFileWithoutParts(0, 10);

        repository.savePart(0, 1);

        assertThatThrownBy(() -> repository.savePart(0, 1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void saving_part_outside_of_parts_interval_results_in_exception() throws IOException {
        repository.addFileWithoutParts(0, 10);

        assertThatThrownBy(() -> repository.savePart(0, 10))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> repository.savePart(0, -1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void listFiles_returns_added_files() throws IOException {
        repository.addFileWithoutParts(1, 10);
        repository.addFileWithoutParts(2, 20);
        repository.addFileWithoutParts(3, 30);

        assertThat(repository.listFiles())
            .extracting("fileId")
            .containsExactly(1, 2, 3);

        assertThat(repository.listFiles())
            .extracting("numberOfParts")
            .containsExactly(10, 20, 30);
    }
}