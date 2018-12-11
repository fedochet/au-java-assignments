package org.anstreth.torrent.client.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface LocalFilesManager {
    /**
     * Registers already existing file.
     */
    void registerFile(int fileId, Path path) throws IOException;

    /**
     * Allocates file with required size, but marks all its parts as empty.
     */
    void allocateFile(int fileId, String name, long size) throws IOException;
    List<FilePartsDetails> listFiles() throws IOException;
    Set<FilePart> getAvailableParts(int fileId) throws IOException;
    InputStream openForReading(FilePart part) throws IOException;
    OutputStream openForWriting(FilePart part) throws IOException;
    void finishFilePart(FilePart part) throws IOException;
}
