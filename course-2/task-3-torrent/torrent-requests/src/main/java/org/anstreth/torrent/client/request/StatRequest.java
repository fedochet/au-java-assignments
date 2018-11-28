package org.anstreth.torrent.client.request;

public class StatRequest {
    private final int fileId;

    public StatRequest(int fileId) {
        this.fileId = fileId;
    }

    public int getFileId() {
        return fileId;
    }
}
