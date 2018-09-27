package ru.hse.spb.git;

import lombok.Data;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import ru.hse.spb.git.blob.FileBlobRepository;
import ru.hse.spb.git.commit.Commit;
import ru.hse.spb.git.commit.CommitInfo;
import ru.hse.spb.git.commit.CommitRepository;
import ru.hse.spb.git.filetree.FileRef;
import ru.hse.spb.git.filetree.FileTree;
import ru.hse.spb.git.filetree.FileTreeRepository;
import ru.hse.spb.git.index.IndexManager;
import ru.hse.spb.git.index.IndexRecord;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.hse.spb.git.CollectionUtils.ioFunction;
import static ru.hse.spb.git.CollectionUtils.ioPredicate;

interface Status {
    @NotNull
    Set<Path> getCommittedFiles();
    @NotNull
    Set<Path> getStagedFiles();
    @NotNull
    Set<Path> getNotStagedFiles();
    @NotNull
    Set<Path> getRemovedFiles();
    @NotNull
    Set<Path> getNotTrackedFiles();
}

@Data
final class StatusBuilder implements Status {
    private final Set<Path> committedFiles = new HashSet<>();
    private final Set<Path> stagedFiles = new HashSet<>();
    private final Set<Path> notStagedFiles = new HashSet<>();
    private final Set<Path> removedFiles = new HashSet<>();
    private final Set<Path> notTrackedFiles = new HashSet<>();

    public StatusBuilder withCommittedFiles(Path... path) {
        committedFiles.addAll(Arrays.asList(path));
        return this;
    }

    public StatusBuilder withStagedFiles(Path... path) {
        stagedFiles.addAll(Arrays.asList(path));
        return this;
    }

    public StatusBuilder withNotStagedFiles(Path... paths) {
        notStagedFiles.addAll(Arrays.asList(paths));
        return this;
    }

    public StatusBuilder withRemovedFiles(Path... paths) {
        removedFiles.addAll(Arrays.asList(paths));
        return this;
    }

    public StatusBuilder withNotTrackedFiles(Path... paths) {
        notTrackedFiles.addAll(Arrays.asList(paths));
        return this;
    }
}


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

        if (onTipOfTheMaster()) {
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

        removeTreeInDir(currentFileTree, repositoryRoot);
        restoreTreeInDir(targetFileTree, repositoryRoot);

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
        catchRemovedFromIndex(statusBuilder);

        return statusBuilder;
    }

    @TestOnly
    public Set<IndexRecord> getCurrentIndex() throws IOException {
        return new HashSet<>(indexManager.getAllRecords());
    }

    private void catchRemovedFromIndex(StatusBuilder statusBuilder) throws IOException {
        for (IndexRecord record : indexManager.getAllRecords()) {
            if (!Files.exists(repositoryRoot.resolve(record.getPath()))) {
                statusBuilder.withRemovedFiles(repositoryRoot.resolve(record.getPath()));
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
        Optional<String> indexVersion = indexManager.get(folderFile).map(IndexRecord::getHash);
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
            throw new IllegalArgumentException("File " + folderFile + " is committed, but not in index!");
        }
    }

    private Optional<String> getCurrentCommitVersionOfFile(Path folderFile) throws IOException {
        Path relativePath = repositoryRoot.relativize(folderFile);

        return getHeadCommit().flatMap(ioFunction(hash -> {
            Commit headCommit = getExistingCommit(hash);
            FileTree currentTree = getExistingTree(headCommit.getTreeHash());
            return locateInTree(currentTree, relativePath.iterator());
        }));
    }

    private Optional<String> locateInTree(FileTree currentLevel, Iterator<Path> pathPieces) throws IOException {
        if (!pathPieces.hasNext()) {
            return Optional.empty();
        }

        Path current = pathPieces.next();
        for (FileRef child : currentLevel.getChildren()) {
            if (child.getName().equals(current.getFileName().toString())) {
                if (child.getType().equals(FileRef.Type.DIRECTORY)) {
                    return locateInTree(getExistingTree(child.getHash()), pathPieces);
                } else if (!pathPieces.hasNext()) {
                    return Optional.of(child.getHash());
                }
            }
        }

        return Optional.empty();
    }

    private boolean onTipOfTheMaster() throws IOException {
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
        return buildTree(repositoryRoot).orElseThrow(() ->
            new IllegalArgumentException("Cannot build tree without files!")
        );
    }

    private Optional<String> buildTree(Path folder) throws IOException {
        List<Path> folderFiles = getFolderFiles(folder);

        List<FileRef> refs = new ArrayList<>();
        for (Path folderFile : folderFiles) {
            String fileName = folderFile.getFileName().toString();

            if (Files.isDirectory(folderFile)) {
                buildTree(folderFile).ifPresent(treeHash ->
                    refs.add(new FileRef(treeHash, FileRef.Type.DIRECTORY, fileName))
                );
            } else {
                String blobHash = blobRepository.hashBlob(folderFile);
                if (indexManager.get(folderFile).isPresent()) {
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

    private List<Path> getFolderFiles(Path folder) throws IOException {
        try (Stream<Path> list = Files.list(folder)) {
            return list
                .filter(ioPredicate(f -> !Files.isSameFile(f, metadataDir)))
                .collect(Collectors.toList());
        }
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
                Path targetFile = dir.resolve(child.getName());
                Files.deleteIfExists(targetFile);
                indexManager.delete(targetFile);
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
                    indexManager.set(targetFile, child.getHash());
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
}
