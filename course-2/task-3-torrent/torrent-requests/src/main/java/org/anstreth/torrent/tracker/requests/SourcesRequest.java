package org.anstreth.torrent.tracker.requests;

public class SourcesRequest {
    private final int id;

    public SourcesRequest(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
