package ru.hse.spb.git;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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
        String treeHash = buildRootTree().getHash();
        Commit commit = commitRepository.createCommit(treeHash, commitMessage);

        updateHead(commit);

        return commit.getHash();
    }

    private void updateHead(Commit commit) throws IOException {
        FileUtils.write(head.toFile(), commit.getHash(), "UTF-8");
    }

    private FileTree buildRootTree() throws IOException {
        return buildTree(repositoryRoot).orElseThrow(() ->
            new IllegalArgumentException("Cannot build tree without files!")
        );
    }

    Optional<FileTree> buildTree(Path folder) throws IOException {
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
                buildTree(folderFile).ifPresent(tree ->
                    refs.add(new FileRef(tree.getHash(), FileRef.Type.DIRECTORY, fileName))
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

        return Optional.of(fileTreeRepository.createTree(refs));
    }

    @NotNull
    public List<Object> getLog() {
        return Collections.emptyList();
    }

    public void resetTo(String hash) throws IOException {
        Commit targetCommit = commitRepository.getCommit(hash)
            .orElseThrow(() -> new IllegalArgumentException("No commit with " + hash + " found!"));

        FileTree fileTree = getExistingTree(targetCommit.getTreeHash());

        restoreTreeInDir(fileTree, repositoryRoot);
    }

    private void restoreTreeInDir(FileTree fileTree, Path dir) throws IOException {
        checkFileTreeIsValid(fileTree);

        for (FileRef child : fileTree.getChildren()) {
            if (child.getType().equals(FileRef.Type.DIRECTORY)) {
                Files.createDirectories(dir.resolve(child.getName()));
                FileTree childFileTree = getExistingTree(child.getHash());

                restoreTreeInDir(childFileTree, dir.resolve(child.getName()));
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
            new IllegalArgumentException("No blob with hash " + hash + "found!")
        );
    }

    @NotNull
    private FileTree getExistingTree(String hash) throws IOException {
        return fileTreeRepository.getTree(hash).orElseThrow(() ->
            new IllegalArgumentException("No tree with hash " + hash + "found!")
        );
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
