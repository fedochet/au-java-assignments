package ru.hse.spb.git;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RepositoryManager {
    @Getter
    private final Path repositoryRoot;
    private final Path objectsDir;

    private final FileBlobRepository blobRepository;
    private final FileTreeRepository fileTreeRepository;
    private final CommitRepository commitRepository;

    public RepositoryManager(Path repositoryRoot) throws IOException {
        this.repositoryRoot = repositoryRoot;
        Path metadataDir = Files.createDirectories(repositoryRoot.resolve(".mygit"));
        objectsDir = Files.createDirectories(metadataDir.resolve("objects"));

        blobRepository = new FileBlobRepository(objectsDir);
        fileTreeRepository = new FileTreeRepository(objectsDir);
        commitRepository = new CommitRepository(objectsDir);
    }
}
