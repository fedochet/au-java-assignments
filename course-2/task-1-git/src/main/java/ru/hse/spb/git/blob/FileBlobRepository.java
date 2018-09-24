package ru.hse.spb.git.blob;

import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@AllArgsConstructor
public class FileBlobRepository {

    private static final Charset ENCODING = StandardCharsets.UTF_8;
    private static final String MARKER = "file\0";
    private static final int MARKER_LENGTH = MARKER.getBytes().length;

    private final Path root;

    public Optional<InputStream> getBlob(String hash) throws IOException {
        Path file = root.resolve(hash);
        if (!Files.exists(file)) {
            return Optional.empty();
        }

        InputStream blobInputStream = Files.newInputStream(file);
        blobInputStream.skip(MARKER_LENGTH);

        return Optional.of(blobInputStream);
    }

    public boolean exists(String hash) throws IOException {
        Path blobFile = root.resolve(hash);

        return Files.exists(blobFile) && MARKER.equals(readMarker(blobFile));
    }

    @NotNull
    public String createBlob(Path file) throws IOException {
        String hash = hashBlob(file);
        if (exists(hash)) {
            throw new IllegalArgumentException("Blob with " + hash + " hash already exists!");
        }

        Path blobFile = Files.createFile(root.resolve(hash));

        try (InputStream stream = withMarker(Files.newInputStream(file))) {
            Files.copy(stream, blobFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return hash;
    }


    @NotNull
    public String hashBlob(Path blob) throws IOException {
        try (InputStream inputStream = Files.newInputStream(blob)) {
            return DigestUtils.sha1Hex(withMarker(inputStream));
        }
    }

    private InputStream withMarker(InputStream blob) {
        return new SequenceInputStream(
            IOUtils.toInputStream(MARKER, ENCODING),
            blob
        );
    }

    @NotNull
    private String readMarker(Path resolve) throws IOException {
        try (InputStream inputStream = Files.newInputStream(resolve)) {
            byte[] bytes = new byte[MARKER_LENGTH];
            inputStream.read(bytes);

            return new String(bytes, ENCODING);
        }
    }
}
