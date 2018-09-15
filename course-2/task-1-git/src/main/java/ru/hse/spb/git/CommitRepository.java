package ru.hse.spb.git;

import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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

        InputStream encodedCommit = Files.newInputStream(file);
        assert MARKER_LENGTH == encodedCommit.skip(MARKER_LENGTH) : "No " + MARKER + " present in file!";

        return Optional.of(decodeCommit(encodedCommit));
    }

    public boolean exists(String hash) {
        return Files.exists(root.resolve(hash));
    }

    public String createCommit(String fileTreeHash, String message) throws IOException {
        return null;
    }

    public String hashCommit(String fileTreeHash, String message) throws IOException {
        return null;
    }

    private InputStream withMarker(InputStream blob) throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(MARKER, ENCODING),
            blob
        );
    }

    private Commit decodeCommit(InputStream encodedCommit) {
        return null;
    }

}
