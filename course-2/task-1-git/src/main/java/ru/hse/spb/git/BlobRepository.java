package ru.hse.spb.git;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

interface BlobRepository {
    Optional<InputStream> getBlob(String hash) throws IOException;
    boolean exists(String hash);
    String createBlob(IOSupplier<InputStream> dataSupplier) throws IOException;
    String hashBlob(InputStream blob) throws IOException;

    @FunctionalInterface
    interface IOSupplier<T> {
        T get() throws IOException;
    }
}
