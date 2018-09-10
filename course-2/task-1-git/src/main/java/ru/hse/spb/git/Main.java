package ru.hse.spb.git;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

    @NotNull
    String hashBlob(Path path) throws IOException;
    @NotNull
    byte[] getBlobContent(@NotNull Blob blob) throws IOException;

    @NotNull
    Blob createBlob(Path path) throws IOException;
    @NotNull
    FileTree createFileTree(String name, List<String> content);
    @NotNull
    Commit createCommit(String message, Path file, @Nullable String parentHash);
}

class FileBasedRepository implements Repository {

    enum BlobMarker {
        BLOB
    }

    private static final String OBJECTS_DIR = "objects";
    private static final String HEAD_FILE = "HEAD";

    @Getter
    private final Path rootDirectory;
    private final Path metadataDirectory;
    private final Path headFile;
    private final Path objectsDirectory;

    private FileBasedRepository(@NotNull Path rootDirectory) {
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

    @Override
    public Optional<String> getHead() {
        return Optional.empty();
    }

    @Override
    public Optional<Blob> getBlob(String hash) {
        Path blobFile = objectsDirectory.resolve(hash);

        if (Files.exists(blobFile)) {
            return Optional.of(new Blob(hash, blobFile));
        }

        return Optional.empty();
    }

    @Override
    public Optional<FileTree> getFileTree(String hash) {
        return Optional.empty();
    }

    @Override
    public Optional<Commit> getCommit(String hash) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public String hashBlob(Path path) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            writeMarker(buffer, BlobMarker.BLOB);
            buffer.write(Files.readAllBytes(path));

            return DigestUtils.shaHex(buffer.toByteArray());
        }
    }

    @NotNull
    @Override
    public byte[] getBlobContent(@NotNull Blob blob) throws IOException {
        try (InputStream inputStream = Files.newInputStream(blob.getFile())) {
            readMarker(inputStream);
            return IOUtils.toByteArray(inputStream);
        }
    }

    @NotNull
    @Override
    public Blob createBlob(Path sourceFile) throws IOException {
        String hash = hashBlob(sourceFile);

        getBlob(hash).ifPresent((blob) -> {
            throw new IllegalArgumentException("Object with hash " + hash + " is already present: " + blob);
        });

        Path blobFile = Files.createFile(objectsDirectory.resolve(hash));

        try (OutputStream outputStream = Files.newOutputStream(blobFile, StandardOpenOption.APPEND)) {
            writeMarker(outputStream, BlobMarker.BLOB);
            Files.copy(sourceFile, outputStream);
        }

        return new Blob(hash, blobFile);
    }

    @NotNull
    @Override
    public FileTree createFileTree(String name, List<String> content) {
        return null;
    }

    @NotNull
    @Override
    public Commit createCommit(String message, Path file, @Nullable String parentHash) {
        return null;
    }

    private static BlobMarker readMarker(InputStream stream) throws IOException {
        int i;
        ByteOutputStream buffer = new ByteOutputStream();

        while ((i = stream.read()) != -1) {
            if (i == 0) {
                break;
            }

            buffer.write(i);
        }

        return BlobMarker.valueOf(buffer.toString());
    }

    private static void writeMarker(OutputStream outputStream, BlobMarker marker) throws IOException {
        outputStream.write((marker.name() + "\0").getBytes());
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}