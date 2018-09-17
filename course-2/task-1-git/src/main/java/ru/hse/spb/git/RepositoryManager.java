package ru.hse.spb.git;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
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
        if (!Files.exists(head)) {
            Files.createFile(head);
        }

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
        String treeHash = buildRootTree();
        Commit commit = commitRepository.createCommit(treeHash, commitMessage, getHeadCommit().orElse(null));

        updateHead(commit);

        return commit.getHash();
    }

    public List<CommitInfo> getLog() throws IOException {
        Optional<String> headCommit = getHeadCommit();
        if (!headCommit.isPresent()) {
            return Collections.emptyList();
        }

        List<CommitInfo> log = new ArrayList<>();
        iterateFrom(headCommit.get()).forEachRemaining(c ->
            log.add(new CommitInfo(c.getHash(), c.getMessage()))
        );

        return log;
    }

    public Optional<String> getHeadCommit() throws IOException {
        String hash = FileUtils.readFileToString(head.toFile(), "UTF-8");
        return Optional.of(hash).filter(s -> !s.isEmpty());
    }

    public void checkoutTo(String hash) throws IOException {
        Commit currentCommit = getExistingCommit(getHeadCommit().get());
        Commit targetCommit = getExistingCommit(hash);

        FileTree currentFileTree = getExistingTree(currentCommit.getTreeHash());
        FileTree targetFileTree = getExistingTree(targetCommit.getTreeHash());

        removeTreeInDir(currentFileTree, repositoryRoot);
        restoreTreeInDir(targetFileTree, repositoryRoot);
    }

    private Iterator<Commit> iterateFrom(String hash) throws IOException {
        return new Iterator<Commit>() {
            Commit next = commitRepository.getCommit(hash).orElse(null);

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            @SneakyThrows
            public Commit next() {
                Commit current = next;
                Optional<String> parent = next.getParentHash();
                if (parent.isPresent()) {
                    next = getExistingCommit(parent.get());
                } else {
                    next = null;
                }

                return current;
            }
        };
    }

    private void updateHead(Commit commit) throws IOException {
        FileUtils.write(head.toFile(), commit.getHash(), "UTF-8");
    }

    private String buildRootTree() throws IOException {
        return buildTree(repositoryRoot).orElseThrow(() ->
            new IllegalArgumentException("Cannot build tree without files!")
        );
    }

    private Optional<String> buildTree(Path folder) throws IOException {
        final List<Path> folderFiles;

        try (Stream<Path> list = Files.list(folder)) {
            folderFiles = list
                .filter(ioPredicate(f -> !Files.isSameFile(f, metadataDir)))
                .collect(Collectors.toList());
        }

        List<FileRef> refs = new ArrayList<>();
        for (Path folderFile : folderFiles) {
            String fileName = folderFile.getFileName().toString();

            if (Files.isDirectory(folderFile)) {
                buildTree(folderFile).ifPresent(treeHash ->
                    refs.add(new FileRef(treeHash, FileRef.Type.DIRECTORY, fileName))
                );
            } else {
                String blobHash = blobRepository.hashBlob(folderFile);
                if (blobRepository.exists(blobHash)) {
                    refs.add(new FileRef(blobHash, FileRef.Type.REGULAR_FILE, fileName));
                }
            }
        }

        if (refs.isEmpty()) {
            return Optional.empty();
        }

        String treeHash = fileTreeRepository.hashTree(refs);
        if (fileTreeRepository.exists(treeHash)) {
            return Optional.of(treeHash);
        }

        return Optional.of(fileTreeRepository.createTree(refs).getHash());
    }

    private void removeTreeInDir(FileTree fileTree, Path dir) throws IOException {
        checkFileTreeIsValid(fileTree);

        for (FileRef child : fileTree.getChildren()) {
            if (child.getType().equals(FileRef.Type.DIRECTORY)) {
                Path targetDir = dir.resolve(child.getName());
                Files.createDirectories(targetDir);
                FileTree childFileTree = getExistingTree(child.getHash());

                removeTreeInDir(childFileTree, targetDir);
            } else {
                // TODO 17.09.2018: reject checkout if file is removed
                Files.deleteIfExists(dir.resolve(child.getName()));
            }
        }

        if (Files.list(dir).count() == 0) {
            Files.delete(dir);
        }
    }

    private void restoreTreeInDir(FileTree fileTree, Path dir) throws IOException {
        checkFileTreeIsValid(fileTree);

        for (FileRef child : fileTree.getChildren()) {
            if (child.getType().equals(FileRef.Type.DIRECTORY)) {
                Path targetDir = dir.resolve(child.getName());
                Files.createDirectories(targetDir);
                FileTree childFileTree = getExistingTree(child.getHash());

                restoreTreeInDir(childFileTree, targetDir);
            } else {
                try (InputStream blobInputStream = getExistingBlob(child.getHash())) {
                    Path targetFile = dir.resolve(child.getName());
                    Files.copy(blobInputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    @NotNull
    private InputStream getExistingBlob(String hash) throws IOException {
        return blobRepository.getBlob(hash).orElseThrow(() ->
            new IllegalArgumentException("No blob with hash " + hash + " found!")
        );
    }

    @NotNull
    private FileTree getExistingTree(String hash) throws IOException {
        return fileTreeRepository.getTree(hash).orElseThrow(() ->
            new IllegalArgumentException("No tree with hash " + hash + " found!")
        );
    }

    @NotNull
    private Commit getExistingCommit(String hash) throws IOException {
        return commitRepository.getCommit(hash).orElseThrow(() ->
            new IllegalArgumentException("No commit with hash " + hash + " found!")
        );
    }

    private void checkFileTreeIsValid(FileTree fileTree) throws IOException {
        for (FileRef child : fileTree.getChildren()) {
            if (child.getType().equals(FileRef.Type.REGULAR_FILE)) {
                if (!blobRepository.exists(child.getHash())) {
                    throw new IllegalArgumentException(String.format(
                        "File tree with hash %s is invalid; file reference %s points to non-existent file.",
                        fileTree.getHash(),
                        child
                    ));
                }
            } else {
                if (!fileTreeRepository.exists(child.getHash())) {
                    throw new IllegalArgumentException(String.format(
                        "File tree with hash %s is invalid; tree reference %s points to non-existent file tree.",
                        fileTree.getHash(),
                        child
                    ));
                }
            }
        }
    }

    private interface IOPredicate<T> {
        @SneakyThrows
        boolean test(T t) throws IOException;
    }

    private interface IOFunction<F, T> {
        T apply(F t) throws IOException;
    }

    private <T> Predicate<T> ioPredicate(IOPredicate<T> p) {
        return t -> {
            try {
                return p.test(t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private <F, T> Function<F, T> ioFunction(IOFunction<F, T> p) {
        return t -> {
            try {
                return p.apply(t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
