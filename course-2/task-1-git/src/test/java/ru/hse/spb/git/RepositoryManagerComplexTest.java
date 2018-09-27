package ru.hse.spb.git;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.hse.spb.git.index.FileReference;
import ru.hse.spb.git.status.StatusBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.*;

public class RepositoryManagerComplexTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private RepositoryManager repository;

    @Before
    public void initRepository() throws IOException {
        repository = RepositoryManager.init(tempFolder.getRoot().toPath());
    }

    @Test
    public void sequential_commits_with_additions() throws IOException {
        Path repositoryRoot = repository.getRepositoryRoot();

        Path f1 = createFile("f1", "f1 initial");
        Path f2 = createFile(Paths.get("dir1", "f2"), "f2 initial");
        Path f3 = createFile(Paths.get("dir2", "dir3", "f3"), "f3 initial");

        Path relativeF1 = repositoryRoot.relativize(f1);
        Path relativeF2 = repositoryRoot.relativize(f2);
        Path relativeF3 = repositoryRoot.relativize(f3);

        assertThat(repository.getCurrentIndex()).isEmpty();
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withNotTrackedFiles(f1, f2, f3)
        );

        String hash1 = repository.addFile(f1);
        assertThat(repository.getCurrentIndex()).containsExactly(
            FileReference.fromPath(hash1, relativeF1)
        );
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withStagedFiles(f1)
                .withNotTrackedFiles(f2, f3)
        );

        String commit1 = repository.commit("commit 1");
        assertThat(repository.getCurrentIndex()).containsExactlyInAnyOrder(
            FileReference.fromPath(hash1, relativeF1)
        );
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withCommittedFiles(f1)
                .withNotTrackedFiles(f2, f3)
        );

        String hash2 = repository.addFile(f2);
        assertThat(repository.getCurrentIndex()).containsExactlyInAnyOrder(
            FileReference.fromPath(hash1, relativeF1),
            FileReference.fromPath(hash2, relativeF2)
        );
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withCommittedFiles(f1)
                .withStagedFiles(f2)
                .withNotTrackedFiles(f3)
        );

        String commit2 = repository.commit("commit 2");
        assertThat(repository.getCurrentIndex()).containsExactlyInAnyOrder(
            FileReference.fromPath(hash1, relativeF1),
            FileReference.fromPath(hash2, relativeF2)
        );
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withCommittedFiles(f1, f2)
                .withNotTrackedFiles(f3)
        );

        String hash3 = repository.addFile(f3);
        assertThat(repository.getCurrentIndex()).containsExactlyInAnyOrder(
            FileReference.fromPath(hash1, relativeF1),
            FileReference.fromPath(hash2, relativeF2),
            FileReference.fromPath(hash3, relativeF3)
        );
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withCommittedFiles(f1, f2)
                .withStagedFiles(f3)
        );

        String commit3 = repository.commit("commit 3");
        assertThat(repository.getCurrentIndex()).containsExactlyInAnyOrder(
            FileReference.fromPath(hash1, relativeF1),
            FileReference.fromPath(hash2, relativeF2),
            FileReference.fromPath(hash3, relativeF3)
        );
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withCommittedFiles(f1, f2, f3)
        );

        FileUtils.write(f1.toFile(), "f1 changed", UTF_8);
        assertThat(repository.getCurrentIndex()).containsExactlyInAnyOrder(
            FileReference.fromPath(hash1, relativeF1),
            FileReference.fromPath(hash2, relativeF2),
            FileReference.fromPath(hash3, relativeF3)
        );
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withNotStagedFiles(f1)
                .withCommittedFiles(f2, f3)
        );

        String modifiedHash1 = repository.addFile(f1);
        assertThat(repository.getCurrentIndex()).containsExactlyInAnyOrder(
            FileReference.fromPath(modifiedHash1, relativeF1),
            FileReference.fromPath(hash2, relativeF2),
            FileReference.fromPath(hash3, relativeF3)
        );
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withStagedFiles(f1)
                .withCommittedFiles(f2, f3)
        );

        String commit4 = repository.commit("commit 4");
        assertThat(repository.getCurrentIndex()).containsExactlyInAnyOrder(
            FileReference.fromPath(modifiedHash1, relativeF1),
            FileReference.fromPath(hash2, relativeF2),
            FileReference.fromPath(hash3, relativeF3)
        );
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withCommittedFiles(f1, f2, f3)
        );

        repository.checkoutToCommit(commit1);
        assertThat(repository.getCurrentIndex()).containsExactlyInAnyOrder(
            FileReference.fromPath(hash1, relativeF1)
        );
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withCommittedFiles(f1)
        );
        assertThat(f1).hasContent("f1 initial");
        assertThat(f2).doesNotExist();
        assertThat(f3).doesNotExist();

        repository.checkoutToCommit(commit2);
        assertThat(repository.getCurrentIndex()).containsExactlyInAnyOrder(
            FileReference.fromPath(hash1, relativeF1),
            FileReference.fromPath(hash2, relativeF2)
        );
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withCommittedFiles(f1, f2)
        );
        assertThat(f1).hasContent("f1 initial");
        assertThat(f2).hasContent("f2 initial");
        assertThat(f3).doesNotExist();

        repository.checkoutToCommit(commit3);
        assertThat(repository.getCurrentIndex()).containsExactlyInAnyOrder(
            FileReference.fromPath(hash1, relativeF1),
            FileReference.fromPath(hash2, relativeF2),
            FileReference.fromPath(hash3, relativeF3)
        );
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withCommittedFiles(f1, f2, f3)
        );
        assertThat(f1).hasContent("f1 initial");
        assertThat(f2).hasContent("f2 initial");
        assertThat(f3).hasContent("f3 initial");

        repository.checkoutToCommit(commit4);
        assertThat(repository.getCurrentIndex()).containsExactlyInAnyOrder(
            FileReference.fromPath(modifiedHash1, relativeF1),
            FileReference.fromPath(hash2, relativeF2),
            FileReference.fromPath(hash3, relativeF3)
        );
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withCommittedFiles(f1, f2, f3)
        );
        assertThat(f1).hasContent("f1 changed");
        assertThat(f2).hasContent("f2 initial");
        assertThat(f3).hasContent("f3 initial");
    }

    @Test
    public void sequential_commits_with_deletions() throws IOException {
        Path repositoryRoot = repository.getRepositoryRoot();

        Path f1 = createFile("f1", "f1 initial");
        Path f2 = createFile(Paths.get("dir1", "f2"), "f2 initial");
        Path f3 = createFile(Paths.get("dir2", "dir3", "f3"), "f3 initial");

        Path relativeF1 = repositoryRoot.relativize(f1);
        Path relativeF2 = repositoryRoot.relativize(f2);
        Path relativeF3 = repositoryRoot.relativize(f3);

        repository.addFile(f1);
        repository.addFile(f2);
        repository.addFile(f3);
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder().withStagedFiles(f1, f2, f3)
        );

        repository.commit("commit 1");
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder().withCommittedFiles(f1, f2, f3)
        );

        Files.delete(f1);
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .withMissingFiles(f1)
                .withCommittedFiles(f2, f3)
        );
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
