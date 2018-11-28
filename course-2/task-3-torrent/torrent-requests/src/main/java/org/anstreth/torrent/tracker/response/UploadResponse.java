package org.anstreth.torrent.tracker.response;

public class UploadResponse {
    private final int id;

    public UploadResponse(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
