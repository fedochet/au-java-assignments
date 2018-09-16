package ru.hse.spb.git;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepositoryManager {
    @Getter
    private final Path repositoryRoot;
    private final Path metadataDir;
    private final Path objectsDir;
    private final Path head;

    private final FileBlobRepository blobRepository;
    private final FileTreeRepository fileTreeRepository;
    private final CommitRepository commitRepository;

    public RepositoryManager(Path repositoryRoot) throws IOException {
        this.repositoryRoot = repositoryRoot;
        metadataDir = Files.createDirectories(repositoryRoot.resolve(".mygit"));
        objectsDir = Files.createDirectories(metadataDir.resolve("objects"));
        head = metadataDir.resolve("HEAD");

        blobRepository = new FileBlobRepository(objectsDir);
        fileTreeRepository = new FileTreeRepository(objectsDir);
        commitRepository = new CommitRepository(objectsDir);
    }

    public String commitFile(Path newFile, String commitMessage) throws IOException {
        String hash = blobRepository.hashBlob(newFile);
        if (blobRepository.exists(hash)) {
            return hash;
        }

        blobRepository.createBlob(newFile);
        String treeHash = buildTree();
        Commit commit = commitRepository.createCommit(treeHash, commitMessage);

        updateHead(commit);

        return commit.getHash();
    }

    private void updateHead(Commit commit) throws IOException {
        FileUtils.write(head.toFile(), commit.getHash(), "UTF-8");
    }

    private String buildTree() throws IOException {
        final List<Path> rootFiles;

        try (Stream<Path> list = Files.list(repositoryRoot)) {
            rootFiles = list.filter(ioPredicate(f -> !Files.isSameFile(f, metadataDir)))
                .filter(f -> !Files.isDirectory(f))
                .collect(Collectors.toList());
        }

        List<FileRef> rootFilesRefs = rootFiles.stream()
            .filter(ioPredicate(f -> blobRepository.exists(blobRepository.hashBlob(f))))
            .map(ioFunction(f -> new FileRef(
                blobRepository.hashBlob(f),
                FileRef.Type.REGULAR_FILE,
                f.getFileName().toString())
            ))
            .collect(Collectors.toList());

        return fileTreeRepository.createTree(rootFilesRefs).getHash();

    }

    @NotNull
    public List<Object> getLog() {
        return Collections.emptyList();
    }

    public void resetTo(String hash) throws IOException {
        Commit targetCommit = commitRepository.getCommit(hash)
            .orElseThrow(() -> new IllegalArgumentException("No commit with " + hash + " found!"));

        FileTree fileTree = fileTreeRepository.getTree(targetCommit.getTreeHash())
            .orElseThrow(() -> new IllegalArgumentException("No tree with hash " + hash + "found!"));

        checkFileTreeIsValid(fileTree);

        for (FileRef child : fileTree.getChildren()) {
            if (child.getType().equals(FileRef.Type.DIRECTORY)) {
                continue;
            }

            try (InputStream blobInputStream = blobRepository.getBlob(child.getHash()).get()) {
                Path targetFile = repositoryRoot.resolve(child.getName());
                Files.copy(blobInputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void checkFileTreeIsValid(FileTree fileTree) {
        for (FileRef child : fileTree.getChildren()) {
            if (!blobRepository.exists(child.getHash())) {
                throw new IllegalArgumentException(String.format(
                    "File tree with hash %s is invalid; file reference %s points to non-existent file.",
                    fileTree.getHash(),
                    child
                ));
            }
        }
    }

    public Optional<String> getHeadCommit() throws IOException {
        String hash = FileUtils.readFileToString(head.toFile(), "UTF-8");
        return Optional.of(hash).filter(s -> !s.isEmpty());
    }

    interface IOPredicate<T> {
        boolean test(T t) throws IOException;
    }

    interface IOFunction<F, T> {
        T apply(F t) throws IOException;
    }

    <T> Predicate<T> ioPredicate(IOPredicate<T> p) {
        return t -> {
            try {
                return p.test(t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    <F, T> Function<F, T> ioFunction(IOFunction<F, T> p) {
        return t -> {
            try {
                return p.apply(t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
