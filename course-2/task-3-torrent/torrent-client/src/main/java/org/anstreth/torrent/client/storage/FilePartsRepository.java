package org.anstreth.torrent.client.storage;

import java.io.IOException;
import java.util.List;

public interface FilePartsRepository {
    void addFileWithAllParts(int fileId, int numberOfParts) throws IOException;
    void addFileWithoutParts(int fileId, int numberOfParts) throws IOException;
    FilePartsDetails getFile(int fileId) throws IOException;
    void savePart(int fileId, int partNumber) throws IOException;
    List<FilePartsDetails> listFiles() throws IOException;
}
