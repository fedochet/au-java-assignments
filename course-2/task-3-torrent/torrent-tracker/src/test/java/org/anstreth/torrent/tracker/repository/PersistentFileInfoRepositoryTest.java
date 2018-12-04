package org.anstreth.torrent.tracker.repository;

import org.anstreth.torrent.tracker.response.FileInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PersistentFileInfoRepositoryTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private FileInfoRepository repository;
    private Path persistenceFile;

    @Before
    public void setUp() throws IOException {
        File file = temporaryFolder.newFile("file-repository");
        persistenceFile = file.toPath();
        repository = new PersistentFileInfoRepository(persistenceFile);
    }

    @Test
    public void repository_on_empty_file_does_not_have_any_info() {
        assertThat(repository.getAllFiles()).isEmpty();
    }

    @Test
    public void file_can_be_saved_and_then_it_appears_in_list() {
        int fileId = repository.addFile("file1", 1024);

        assertThat(repository.getAllFiles()).containsExactly(
            new FileInfo(fileId, "file1", 1024)
        );
    }

    @Test
    public void many_files_can_be_saved() {
        List<String> names = Arrays.asList("f1", "f2", "f3", "f4");

        for (int i = 0; i < names.size(); i++) {
            repository.addFile(names.get(i), i + 1);
        }

        assertThat(repository.getAllFiles())
            .extracting("name")
            .containsExactlyElementsOf(names);
    }

    @Test
    public void if_file_is_removed_getAllFiles_and_addFile_throws_IllegalStateException() throws IOException {
        Files.delete(persistenceFile);

        assertThatThrownBy(() -> repository.addFile("anyFile", 1234))
            .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> repository.getAllFiles())
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void file_ids_are_unique() {
        Set<Integer> ids = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            ids.add(repository.addFile(i + ".file", 1024));
        }

        assertThat(ids).hasSize(1000);
        assertThat(repository.getAllFiles())
            .extracting("id")
            .containsExactlyInAnyOrderElementsOf(ids);
    }
}