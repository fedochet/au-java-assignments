package org.anstreth.torrent.client.request;

public class GetRequest {
    private final int fileId;
    private final int partNumber;

    public GetRequest(int fileId, int partNumber) {
        this.fileId = fileId;
        this.partNumber = partNumber;
    }

    public int getFileId() {
        return fileId;
    }

    public int getPartNumber() {
        return partNumber;
    }
}
