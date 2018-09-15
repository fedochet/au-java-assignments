package ru.hse.spb.git;

import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CommitRepository {

    private static final String ENCODING = "UTF-8";
    private static final String MARKER = "commit\0";
    private static final int MARKER_LENGTH = MARKER.getBytes().length;

    private final Path root;

    public Optional<Commit> getCommit(String hash) throws IOException {
        Path file = root.resolve(hash);
        if (!Files.exists(file)) {
            return Optional.empty();
        }

        try (InputStream encodedCommit = Files.newInputStream(file)) {
            assert MARKER_LENGTH == encodedCommit.skip(MARKER_LENGTH) : "No " + MARKER + " present in file!";
            return Optional.of(decodeCommit(hash, encodedCommit));
        }
    }

    public boolean exists(String hash) {
        return Files.exists(root.resolve(hash));
    }

    @NotNull
    public Commit createCommit(String fileTreeHash, String message) throws IOException {
        String hash = hashCommit(fileTreeHash, message);
        if (exists(hash)) {
            throw new IllegalArgumentException("Commit with such " + hash + " already exists!");
        }

        Path treeFile = Files.createFile(root.resolve(hash));

        try (InputStream inputStream = withMarker(encodeCommit(fileTreeHash, message))) {
            Files.copy(inputStream, treeFile);
        }

        return new Commit(hash, fileTreeHash, message);
    }

    @NotNull
    public String hashCommit(String fileTreeHash, String message) throws IOException {
        try (InputStream inputStream = withMarker(encodeCommit(fileTreeHash, message))) {
            return DigestUtils.sha1Hex(inputStream);
        }
    }

    private InputStream encodeCommit(String fileTreeHash, String message) throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(String.format("tree %s", fileTreeHash), ENCODING),
            IOUtils.toInputStream(message, ENCODING)
        );
    }

    private InputStream withMarker(InputStream blob) throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(MARKER, ENCODING),
            blob
        );
    }

    private Commit decodeCommit(String hash, InputStream encodedCommit) throws IOException {
        List<String> strings = IOUtils.readLines(encodedCommit, ENCODING);

        String treeLine = strings.get(0);
        String treeHash = treeLine.split(" ")[1];
        String message = strings.stream().skip(1).collect(Collectors.joining(System.getProperty("line.separator")));

        return new Commit(hash, treeHash, message);
    }
}
