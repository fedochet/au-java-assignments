package org.anstreth.torrent.client.storage;

import java.util.Set;

class FilePartsDetails {
    public int getFileId() {
        return fileId;
    }

    public int getNumberOfParts() {
        return numberOfParts;
    }

    public Set<Integer> getReadyParts() {
        return readyParts;
    }

    private final int fileId;
    private final int numberOfParts;
    private final Set<Integer> readyParts;

    FilePartsDetails(int fileId, int numberOfParts, Set<Integer> readyParts) {
        this.fileId = fileId;
        this.numberOfParts = numberOfParts;
        this.readyParts = readyParts;
    }
}
