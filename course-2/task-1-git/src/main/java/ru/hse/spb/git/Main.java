package ru.hse.spb.git;

import lombok.Data;

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
}

interface Repository {
    Optional<String> getHead();
    Optional<Blob> getBlob(String hash);
    Optional<FileTree> getFileTree(String hash);
    Optional<Commit> getCommit(String hash);
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}
