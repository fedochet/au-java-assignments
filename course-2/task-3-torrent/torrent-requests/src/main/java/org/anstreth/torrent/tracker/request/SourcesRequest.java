package org.anstreth.torrent.tracker.request;

public class SourcesRequest {
    private final int fileId;

    public SourcesRequest(int fileId) {
        this.fileId = fileId;
    }

    public int getFileId() {
        return fileId;
    }
}
