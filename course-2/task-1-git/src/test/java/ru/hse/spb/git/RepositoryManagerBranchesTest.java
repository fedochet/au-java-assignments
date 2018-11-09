package ru.hse.spb.git;

import org.junit.Before;
import org.junit.Test;
import ru.hse.spb.git.branch.Branch;
import ru.hse.spb.git.status.StatusBuilder;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryManagerBranchesTest extends TempDirectoryTestBase {
    private RepositoryManager repository;

    @Before
    public void initRepository() throws IOException {
        repository = RepositoryManager.init(tempFolder.getRoot().toPath());
    }

    @Test
    public void initial_branch_is_master() throws IOException {
        assertThat(repository.getStatus().getBranch()).isEqualTo("master");
    }

    @Test(expected = IllegalArgumentException.class)
    public void branch_cannot_be_created_without_first_commit() throws IOException {
        repository.createBranch("b1");
    }

    @Test
    public void branch_can_be_created_after_first_commit() throws IOException {
        Path file1 = createFile("f1", "1");
        repository.addFile(file1);
        String commit1 = repository.commit("c1");

        Branch b1 = repository.createBranch("b1");

        assertThat(b1.getName()).isEqualTo("b1");
        assertThat(b1.getHeadCommit()).isEqualTo(commit1);
    }

    @Test
    public void head_of_branch_is_current_head_commit() throws IOException {
        Path file1 = createFile("f1", "1");

        repository.addFile(file1);
        String commit1 = repository.commit("c1");

        Branch b1 = repository.createBranch("b1");

        assertThat(b1.getName()).isEqualTo("b1");
        assertThat(b1.getHeadCommit()).isEqualTo(commit1);

        createFile(file1, "2");
        String commit2 = repository.commit("c2");

        Branch b2 = repository.createBranch("b2");

        assertThat(b2.getName()).isEqualTo("b2");
        assertThat(b2.getHeadCommit()).isEqualTo(commit2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void you_cannot_checkout_to_non_existent_branch() throws IOException {
        Path file1 = createFile("f1", "1");
        repository.addFile(file1);
        repository.commit("c1");

        repository.checkout("b1");
    }

    @Test
    public void you_can_checkout_to_branch() throws IOException {
        Path file1 = createFile("f1", "1");
        repository.addFile(file1);
        String commit1 = repository.commit("c1");

        repository.createBranch("b1");
        repository.checkout("b1");

        assertThat(repository.getHeadCommit()).contains(commit1);
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .onBranch("b1")
                .onCommit(commit1)
                .withCommittedFiles(file1)
        );
    }

    @Test
    public void checkouting_back_and_forth_between_master_and_single_branch() throws IOException {
        Path file1 = createFile("f1", "1");
        repository.addFile(file1);
        String commit1 = repository.commit("c1");

        repository.createBranch("b1");
        repository.checkout("b1");

        Path file2 = createFile("f2", "2");
        repository.addFile(file2);
        String commit2 = repository.commit("c2");

        repository.checkout("master");
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .onBranch("master")
                .onCommit(commit1)
                .withCommittedFiles(file1)
        );
        assertThat(repository.getLog())
            .extracting("message")
            .containsExactly("c1");

        Path file3 = createFile("f3", "3");
        repository.addFile(file3);
        String commit3 = repository.commit("c3");

        repository.checkout("b1");
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .onBranch("b1")
                .onCommit(commit2)
                .withCommittedFiles(file1, file2)
        );
        assertThat(repository.getLog())
            .extracting("message")
            .containsExactly("c2", "c1");

        repository.checkout("master");
        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .onBranch("master")
                .onCommit(commit3)
                .withCommittedFiles(file1, file3)
        );
        assertThat(repository.getLog())
            .extracting("message")
            .containsExactly("c3", "c1");
    }

    @Test
    public void checkouting_to_commit_works_with_checkout_method() throws IOException {
        Path file1 = createFile("f1", "1");
        repository.addFile(file1);
        String commit1 = repository.commit("c1");

        Path file2 = createFile("f2", "2");
        repository.addFile(file2);
        String commit2 = repository.commit("c2");

        repository.checkout(commit1);

        assertThat(repository.getStatus()).isEqualTo(
            new StatusBuilder()
                .onCommit(commit1)
                .withCommittedFiles(file1)
        );
    }
}