package org.anstreth.torrent.client.storage;

import java.nio.file.Path;
import java.util.Set;

class FilePartsDetails {
    private final int fileId;
    private final Path file;
    private final int numberOfParts;
    private final Set<Integer> readyParts;

    FilePartsDetails(int fileId, Path file, int numberOfParts, Set<Integer> readyParts) {
        this.fileId = fileId;
        this.file = file;
        this.numberOfParts = numberOfParts;
        this.readyParts = readyParts;
    }

    public int getFileId() {
        return fileId;
    }

    public int getNumberOfParts() {
        return numberOfParts;
    }

    public Set<Integer> getReadyParts() {
        return readyParts;
    }

    public Path getFile() {
        return file;
    }
}
