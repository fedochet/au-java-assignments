package ru.hse.spb.git;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import ru.hse.spb.git.blob.FileBlobRepository;
import ru.hse.spb.git.commit.Commit;
import ru.hse.spb.git.commit.CommitInfo;
import ru.hse.spb.git.commit.CommitRepository;
import ru.hse.spb.git.filetree.FileTree;
import ru.hse.spb.git.filetree.FileTreeRepository;
import ru.hse.spb.git.filetree.HashRef;
import ru.hse.spb.git.index.FileReference;
import ru.hse.spb.git.index.IndexManager;
import ru.hse.spb.git.index.VirtualFileTree;
import ru.hse.spb.git.status.Status;
import ru.hse.spb.git.status.StatusBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.hse.spb.git.CollectionUtils.ioFunction;
import static ru.hse.spb.git.CollectionUtils.ioPredicate;

public class RepositoryManager {
    private static final Charset ENCODING = StandardCharsets.UTF_8;
    @Getter
    private final Path repositoryRoot;
    private final Path metadataDir;
    private final Path objectsDir;
    private final Path head;

    private final FileBlobRepository blobRepository;
    private final FileTreeRepository fileTreeRepository;
    private final CommitRepository commitRepository;
    private final IndexManager indexManager;
    private final Path masterHead;
    private final Path index;

    private RepositoryManager(Path repositoryRoot) {
        this.repositoryRoot = repositoryRoot;
        metadataDir = repositoryRoot.resolve(".mygit");
        objectsDir = metadataDir.resolve("objects");
        head = metadataDir.resolve("HEAD");
        masterHead = metadataDir.resolve("masterHEAD");
        index = metadataDir.resolve("index");

        blobRepository = new FileBlobRepository(objectsDir);
        fileTreeRepository = new FileTreeRepository(objectsDir);
        commitRepository = new CommitRepository(objectsDir);
        indexManager = new IndexManager(repositoryRoot, index);
    }

    public static RepositoryManager init(Path repositoryRoot) throws IOException {
        Path metadataDir = Files.createDirectories(repositoryRoot.resolve(".mygit"));
        Files.createDirectories(metadataDir.resolve("objects"));
        Path head = metadataDir.resolve("HEAD");
        if (!Files.exists(head)) {
            FileUtils.write(head.toFile(), "master", ENCODING);
        }

        Path masterHead = metadataDir.resolve("masterHEAD");
        if (!Files.exists(masterHead)) {
            Files.createFile(masterHead);
        }

        Path index = metadataDir.resolve("index");
        if (!Files.exists(index)) {
            Files.createFile(index);
        }

        return new RepositoryManager(repositoryRoot);
    }

    public static Optional<RepositoryManager> open(Path repositoryRoot) {
        Path metadataDir = repositoryRoot.resolve(".mygit");
        Path objectsDir = metadataDir.resolve("objects");
        Path head = metadataDir.resolve("HEAD");
        Path masterHead = metadataDir.resolve("masterHEAD");
        Path index = metadataDir.resolve("index");

        if (Stream.of(metadataDir, objectsDir, head, masterHead, index).allMatch(Files::exists)) {
            return Optional.of(new RepositoryManager(repositoryRoot));
        }

        return Optional.empty();
    }

    @Deprecated
    public String commitFile(Path newFile, String commitMessage) throws IOException {
        addFile(newFile);
        return commit(commitMessage);
    }

    @NotNull
    public String commit(@NotNull String commitMessage) throws IOException {
        String treeHash = buildRootTree();
        Commit commit = commitRepository.createCommit(treeHash, commitMessage, getHeadCommit().orElse(null));

        if (onTipOfMaster()) {
            updateMasterHead(commit);
        } else {
            updateHead(commit);
        }

        return commit.getHash();
    }

    @NotNull
    public String addFile(@NotNull Path newFile) throws IOException {
        String hash = blobRepository.hashBlob(newFile);

        if (!blobRepository.exists(hash)) {
            blobRepository.createBlob(newFile);
        }

        addFileToIndex(newFile);
        return hash;
    }

    public List<CommitInfo> getLog() throws IOException {
        return getHeadCommit()
            .map(ioFunction(this::getLog))
            .orElse(Collections.emptyList());
    }

    public List<CommitInfo> getLog(String hash) throws IOException {
        Optional<Commit> headCommit = commitRepository.getCommit(hash);
        if (!headCommit.isPresent()) {
            return Collections.emptyList();
        }

        return commitsFrom(headCommit.get().getHash())
            .map(c -> new CommitInfo(c.getHash(), c.getMessage()))
            .collect(Collectors.toList());
    }

    public Optional<String> getHeadCommit() throws IOException {
        String hash = FileUtils.readFileToString(head.toFile(), "UTF-8");

        if (hash.equals("master")) {
            return getMasterHeadCommit();
        }

        return Optional.of(hash);
    }

    public void checkoutToCommit(String hash) throws IOException {
        Commit currentCommit = getExistingCommit(getHeadCommit().get());
        Commit targetCommit = getExistingCommit(hash);

        FileTree currentFileTree = getExistingTree(currentCommit.getTreeHash());
        FileTree targetFileTree = getExistingTree(targetCommit.getTreeHash());

        removeTreeInRoot(currentFileTree);
        restoreTreeInRoot(targetFileTree);

        updateHead(getExistingCommit(hash));
    }

    public Optional<String> getMasterHeadCommit() throws IOException {
        String hash = FileUtils.readFileToString(masterHead.toFile(), "UTF-8");

        return Optional.of(hash).filter(s -> !s.isEmpty());
    }

    public void resetTo(String hash) throws IOException {
        checkoutToCommit(hash);
        updateMasterHead(getExistingCommit(hash));
        pointHeadToMaster();
    }

    @NotNull
    public Status getStatus() throws IOException {
        StatusBuilder statusBuilder = new StatusBuilder();
        fillStatusInDir(statusBuilder, repositoryRoot);
        gatherRemovedFiles(statusBuilder);

        return statusBuilder;
    }

    public void remove(Path newFile) throws IOException {
        indexManager.delete(newFile);
    }

    @TestOnly
    public Set<FileReference> getCurrentIndex() throws IOException {
        return new HashSet<>(indexManager.getAllRecords());
    }

    private void gatherRemovedFiles(StatusBuilder statusBuilder) throws IOException {
        Optional<String> headCommit = getHeadCommit();
        if (!headCommit.isPresent()) {
            return;
        }

        String hash = headCommit.get();
        FileTree rootTree = getExistingTree(getExistingCommit(hash).getTreeHash());
        List<FileReference> files = getTreeFilesRecursively(rootTree);
        for (FileReference ref : files) {
            Path fullPath = repositoryRoot.resolve(ref.getPath());
            boolean fileIsRemovedFromDisk = Files.notExists(fullPath);
            boolean fileIsNotInIndex = !indexManager.get(fullPath).isPresent();

            if (fileIsRemovedFromDisk && fileIsNotInIndex) {
                statusBuilder.withDeletedFiles(fullPath);
            } else if (fileIsRemovedFromDisk) {
                statusBuilder.withMissingFiles(fullPath);
            }
        }

    }

    private void fillStatusInDir(StatusBuilder statusBuilder, Path folder) throws IOException {
        List<Path> folderFiles = getFolderFiles(folder);

        for (Path folderFile : folderFiles) {
            if (Files.isDirectory(folderFile)) {
                fillStatusInDir(statusBuilder, folderFile);
            } else {
                String blobHash = blobRepository.hashBlob(folderFile);
                if (!blobRepository.exists(blobHash)) {
                    addNotBlobbedFile(statusBuilder, folderFile);
                } else {
                    addBlobbedFile(statusBuilder, folderFile);
                }
            }
        }

    }

    private void addNotBlobbedFile(StatusBuilder statusBuilder, Path folderFile) throws IOException {
        Optional<String> currentCommitVersionOfFile = getCurrentCommitVersionOfFile(folderFile);
        if (currentCommitVersionOfFile.isPresent()) {
            statusBuilder.withNotStagedFiles(folderFile);
        } else {
            statusBuilder.withNotTrackedFiles(folderFile);
        }
    }

    private void addBlobbedFile(StatusBuilder statusBuilder, Path folderFile) throws IOException {
        Optional<String> indexVersion = indexManager.get(folderFile).map(FileReference::getHash);
        Optional<String> commitVersion = getCurrentCommitVersionOfFile(folderFile);

        if (indexVersion.isPresent()) {
            if (indexVersion.equals(commitVersion)) {
                statusBuilder.withCommittedFiles(folderFile);
            } else {
                statusBuilder.withStagedFiles(folderFile);
            }
        } else if (!commitVersion.isPresent()) {
            statusBuilder.withNotTrackedFiles(folderFile);
        } else {
            statusBuilder.withDeletedFiles(folderFile);
        }
    }

    private Optional<String> getCurrentCommitVersionOfFile(Path folderFile) throws IOException {
        Path relativePath = repositoryRoot.relativize(folderFile);

        return getHeadCommit().flatMap(ioFunction(hash -> {
            Commit headCommit = getExistingCommit(hash);
            FileTree currentTree = getExistingTree(headCommit.getTreeHash());
            return locateInTree(currentTree, relativePath).map(FileReference::getHash);
        }));
    }

    private Optional<FileReference> locateInTree(FileTree currentLevel, Path path) throws IOException {
        return getTreeFilesRecursively(currentLevel).stream()
            .filter(ref -> ref.getPath().equals(path))
            .findFirst();
    }

    private boolean onTipOfMaster() throws IOException {
        return getHeadCommit().equals(getMasterHeadCommit());
    }

    private void addFileToIndex(Path newFile) throws IOException {
        String hash = blobRepository.hashBlob(newFile);
        indexManager.set(newFile, hash);
    }

    private Stream<Commit> commitsFrom(String hash) throws IOException {
        return CollectionUtils.generateStream(
            commitRepository.getCommit(hash).orElse(null),
            c -> c.getParentHash().map(ioFunction(this::getExistingCommit))
        );
    }

    private void updateHead(Commit commit) throws IOException {
        FileUtils.write(head.toFile(), commit.getHash(), "UTF-8");
    }

    private void pointHeadToMaster() throws IOException {
        FileUtils.write(head.toFile(), "master", "UTF-8");
    }

    private void updateMasterHead(Commit commit) throws IOException {
        FileUtils.write(masterHead.toFile(), commit.getHash(), "UTF-8");
    }

    private String buildRootTree() throws IOException {
        VirtualFileTree virtualFileTree = new VirtualFileTree();
        for (FileReference reference : getCurrentIndex()) {
            virtualFileTree.addFile(reference);
        }

        return virtualFileTree.buildFileTree(fileTreeRepository).orElseThrow(() ->
            new IllegalArgumentException("Cannot build tree without files!")
        );
    }

    private List<Path> getFolderFiles(Path folder) throws IOException {
        try (Stream<Path> list = Files.list(folder)) {
            return list
                .filter(ioPredicate(f -> !Files.isSameFile(f, metadataDir)))
                .collect(Collectors.toList());
        }
    }

    private void removeTreeInRoot(FileTree fileTree) throws IOException {
        checkFileTreeIsValid(fileTree);

        for (FileReference fileReference : getTreeFilesRecursively(fileTree)) {
            Path targetFile = repositoryRoot.resolve(fileReference.getPath());
            Path parent = targetFile.getParent();
            Files.deleteIfExists(targetFile);
            indexManager.delete(targetFile);

            if (Files.exists(parent) && Files.list(parent).count() == 0) {
                Files.delete(parent);
            }
        }
    }

    private void restoreTreeInRoot(FileTree fileTree) throws IOException {
        checkFileTreeIsValid(fileTree);

        for (FileReference fileReference : getTreeFilesRecursively(fileTree)) {
            Path targetFile = repositoryRoot.resolve(fileReference.getPath());
            try (InputStream blobInputStream = getExistingBlob(fileReference.getHash())) {
                FileUtils.copyToFile(blobInputStream, targetFile.toFile());
            }
            indexManager.set(targetFile, fileReference.getHash());
        }
    }

    @NotNull
    private List<FileReference> getTreeFilesRecursively(FileTree tree) throws IOException {
        return getTreeFilesRecursively(tree, new ArrayDeque<>());
    }

    @NotNull
    private List<FileReference> getTreeFilesRecursively(FileTree tree, Deque<String> path) throws IOException {
        List<FileReference> files = new ArrayList<>();

        for (HashRef child : tree.getChildren()) {
            path.addLast(child.getName());
            if (child.getType().equals(HashRef.Type.DIRECTORY)) {
                files.addAll(getTreeFilesRecursively(getExistingTree(child.getHash()), path));
            } else {
                files.add(new FileReference(child.getHash(), new ArrayList<>(path)));
            }
            path.removeLast();
        }

        return files;
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
        for (HashRef child : fileTree.getChildren()) {
            if (child.getType().equals(HashRef.Type.FILE)) {
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
}
