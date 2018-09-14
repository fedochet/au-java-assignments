package ru.hse.spb.git;

import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@AllArgsConstructor
class FileBlobRepository implements BlobRepository {

    private static final String ENCODING = "UTF-8";
    private static final String MARKER = "file\0";
    private static final int MARKER_LENGTH = MARKER.getBytes().length;

    private final Path root;

    @Override
    public Optional<InputStream> getBlob(String hash) throws IOException {
        Path file = root.resolve(hash);
        if (!Files.exists(file)) {
            return Optional.empty();
        }

        InputStream blobInputStream = Files.newInputStream(file);
        assert MARKER_LENGTH == blobInputStream.skip(MARKER_LENGTH) : "Cannot skip marker bytes";

        return Optional.of(blobInputStream);
    }

    @Override
    public boolean exists(String hash) {
        return Files.exists(root.resolve(hash));
    }

    @Override
    public String createBlob(IOSupplier<InputStream> dataSupplier) throws IOException {
        try (InputStream inputStream = dataSupplier.get()) {
            String hash = hashBlob(inputStream);
            if (exists(hash)) {
                throw new IllegalArgumentException("Blob with such hash already exists!");
            }

            Path blobFile = Files.createFile(root.resolve(hash));

            try (InputStream stream = dataSupplier.get()) {
                Files.copy(stream, blobFile);
            }

            return hash;
        }
    }

    @Override
    public String hashBlob(InputStream blob) throws IOException {
        return DigestUtils.sha1Hex(new SequenceInputStream(
            IOUtils.toInputStream("file\0", ENCODING),
            blob
        ));
    }
}
