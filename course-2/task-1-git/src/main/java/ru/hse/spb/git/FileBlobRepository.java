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
import java.util.Optional;

@AllArgsConstructor
class FileBlobRepository {

    private static final String ENCODING = "UTF-8";
    private static final String MARKER = "file\0";
    private static final int MARKER_LENGTH = MARKER.getBytes().length;

    private final Path root;

    public Optional<InputStream> getBlob(String hash) throws IOException {
        Path file = root.resolve(hash);
        if (!Files.exists(file)) {
            return Optional.empty();
        }

        InputStream blobInputStream = Files.newInputStream(file);
        assert MARKER_LENGTH == blobInputStream.skip(MARKER_LENGTH) : "No " + MARKER + " present in file!";

        return Optional.of(blobInputStream);
    }

    public boolean exists(String hash) {
        return Files.exists(root.resolve(hash));
    }

    @NotNull
    public String createBlob(Path file) throws IOException {
        String hash = hashBlob(file);
        if (exists(hash)) {
            throw new IllegalArgumentException("Blob with " + hash + " hash already exists!");
        }

        Path blobFile = Files.createFile(root.resolve(hash));

        try (InputStream stream = withMarker(Files.newInputStream(file))) {
            Files.copy(stream, blobFile);
        }

        return hash;
    }


    @NotNull
    public String hashBlob(Path blob) throws IOException {
        try (InputStream inputStream = Files.newInputStream(blob)) {
            return DigestUtils.sha1Hex(withMarker(inputStream));
        }
    }

    private InputStream withMarker(InputStream blob) throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(MARKER, ENCODING),
            blob
        );
    }
}
