package org.anstreth.torrent.tracker.requests;

public class UploadRequest {
    private final String name;
    private final long size;

    public UploadRequest(String name, long size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}
