package ru.hse.spb.git.commit;

import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import ru.hse.spb.git.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

@AllArgsConstructor
public class CommitRepository {

    private static final Charset ENCODING = StandardCharsets.UTF_8;
    private static final String MARKER = "commit\0";
    private static final int MARKER_LENGTH = MARKER.getBytes().length;

    private final Path root;

    public Optional<Commit> getCommit(String hash) throws IOException {
        Path file = root.resolve(hash);
        if (!Files.exists(file)) {
            return Optional.empty();
        }

        try (InputStream encodedCommit = Files.newInputStream(file)) {
            encodedCommit.skip(MARKER_LENGTH);
            return Optional.of(decodeCommit(hash, encodedCommit));
        }
    }

    public boolean exists(String hash) {
        return Files.exists(root.resolve(hash));
    }

    public Commit createCommit(String fileTreeHash, String message, @Nullable String parentHash) throws IOException {
        Instant currentTime = Instant.now();

        String hash = hashCommit(fileTreeHash, currentTime, message, parentHash);
        if (exists(hash)) {
            throw new IllegalArgumentException("Commit with such " + hash + " already exists!");
        }

        Path treeFile = Files.createFile(root.resolve(hash));

        try (InputStream inputStream = withMarker(encodeCommit(fileTreeHash, currentTime, message, parentHash))) {
            Files.copy(inputStream, treeFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return new Commit(hash, fileTreeHash, message, parentHash, currentTime);
    }

    public String hashCommit(String fileTreeHash, Instant commitTime, String message, @Nullable String parentHash) throws IOException {
        try (InputStream inputStream = withMarker(encodeCommit(fileTreeHash, commitTime, message, parentHash))) {
            return DigestUtils.sha1Hex(inputStream);
        }
    }

    private InputStream encodeCommit(String fileTreeHash, Instant commitTime, String message, @Nullable String parentHash) {
        String encoded = String.join(
            System.getProperty("line.separator"),
            String.format("tree %s", fileTreeHash),
            parentHash != null ? String.format("parent %s", parentHash) : "",
            commitTime.toString(),
            message
        );

        return IOUtils.toInputStream(encoded, ENCODING);
    }

    private InputStream withMarker(InputStream blob) {
        return new SequenceInputStream(
            IOUtils.toInputStream(MARKER, ENCODING),
            blob
        );
    }

    private Commit decodeCommit(String hash, InputStream encodedCommit) throws IOException {
        Iterator<String> iterator = IOUtils.readLines(encodedCommit, ENCODING).iterator();

        String treeLine = iterator.next();
        String treeHash = treeLine.split(" ")[1];
        String parentLine = iterator.next();
        String commitTimeLine = iterator.next();

        String parentHash = parentLine.isEmpty() ? null : parentLine.split(" ")[1];
        String message = CollectionUtils.toStream(iterator).collect(joining(System.getProperty("line.separator")));
        Instant commitTime = Instant.parse(commitTimeLine);

        return new Commit(hash, treeHash, message, parentHash, commitTime);
    }
}
