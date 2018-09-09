package ru.hse.spb.git;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

enum ObjectType {
    BLOB, FILE_TREE
}

@Data
final class ObjectRef {
    private final String hash;
    private final ObjectType type;
}

@Data
final class Blob {
    private final String hash;
    private final Path file;
}

@Data
final class FileTree {
    private final String hash;
    private final Path file;
    private final String name;
    private final List<ObjectRef> content; // hashes
}

@Data
final class Commit {
    private final String hash;
    private final Path file;
    private final String message;
    private final List<ObjectRef> content; // hashes
    private final String parentHash; // hash of parent commit

    Optional<String> getParentHash() {
        return Optional.ofNullable(parentHash);
    }
}

interface Repository {
    Optional<String> getHead();
    Optional<Blob> getBlob(String hash);
    Optional<FileTree> getFileTree(String hash);
    Optional<Commit> getCommit(String hash);

    Blob createBlob(Path path);
    FileTree createFileTree(String name, List<String> content);
    Commit createCommit(String message, Path file, @Nullable String parentHash);
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}
