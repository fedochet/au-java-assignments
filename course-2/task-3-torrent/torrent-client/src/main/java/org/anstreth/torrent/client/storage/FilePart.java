package org.anstreth.torrent.client.storage;

public class FilePart {
    private final int fileId;
    private final int number;

    public FilePart(int fileId, int number) {
        this.fileId = fileId;
        this.number = number;
    }

    public int getFileId() {
        return fileId;
    }

    public int getNumber() {
        return number;
    }
}
