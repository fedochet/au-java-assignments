package ru.hse.spb.git;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

interface BlobRepository {
    Optional<InputStream> getBlob(String hash) throws IOException;
    boolean exists(String hash);

    @NotNull
    String createBlob(Path file) throws IOException;
    @NotNull
    String hashBlob(InputStream blob) throws IOException;
}
