package ru.hse.spb.git.branch;

import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class BranchManager {
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    private final Path root;

    public BranchManager(Path root) {
        this.root = root;
    }

    public Branch createBranch(@NotNull String name, @NotNull String parentCommitHash) throws IOException {
        Path branchFile;
        try {
            branchFile = Files.createFile(root.resolve(name));
        } catch (FileAlreadyExistsException e) {
            throw new IllegalArgumentException("Branch with name " + name + " is already exists!", e);
        }

        Files.write(branchFile, parentCommitHash.getBytes(ENCODING));

        return new Branch(name, parentCommitHash);
    }

    public Optional<Branch> findBranch(@NotNull String name) throws IOException {
        Path branchFile = root.resolve(name);
        if (Files.exists(branchFile)) {
            String branchHeadCommit = FileUtils.readFileToString(branchFile.toFile(), ENCODING);
            return Optional.of(new Branch(name, branchHeadCommit));
        }

        return Optional.empty();
    }

    public void updateBranch(@NotNull String name, @NotNull String headCommit) throws IOException {
        Path branchFile = root.resolve(name);
        if (Files.notExists(branchFile)) {
            throw new IllegalArgumentException(String.format("Branch %s does not exist.", name));
        }

        Files.write(branchFile, headCommit.getBytes(ENCODING));
    }
}
