package ru.hse.spb.git;

import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
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

class FileBasedRepository {

    private static final String OBJECTS_DIR = "objects";
    private static final String HEAD_FILE = "HEAD";

    @Getter
    private final Path rootDirectory;
    private final Path metadataDirectory;
    private final Path headFile;
    private final Path objectsDirectory;

    FileBasedRepository(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
        metadataDirectory = rootDirectory.resolve(".mygit");
        headFile = metadataDirectory.resolve(HEAD_FILE);
        objectsDirectory = metadataDirectory.resolve(OBJECTS_DIR);
    }

    @NotNull
    static FileBasedRepository init(@NotNull Path rootDirectory) throws IOException {
        if (!Files.isDirectory(rootDirectory)) {
            throw new IllegalArgumentException(
                "Cannot init git repository in " + rootDirectory.toAbsolutePath().toString()
            );
        }

        Path metadataDirectory = rootDirectory.resolve(".mygit");
        if (Files.exists(metadataDirectory) && !Files.isDirectory(metadataDirectory)) {
            throw new IllegalArgumentException(
                "Cannot use .mygit directory in " + rootDirectory.toAbsolutePath().toString()
            );
        }

        initMetadataDirs(metadataDirectory);

        return new FileBasedRepository(rootDirectory);
    }

    private static void initMetadataDirs(Path metadataDirectory) throws IOException {
        Files.createDirectories(metadataDirectory.resolve(OBJECTS_DIR));
        Files.createFile(metadataDirectory.resolve(HEAD_FILE));
    }

}

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}
