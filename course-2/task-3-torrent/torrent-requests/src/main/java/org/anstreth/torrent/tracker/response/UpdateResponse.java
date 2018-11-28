package org.anstreth.torrent.tracker.response;

public class UpdateResponse {
    private final boolean status;

    public UpdateResponse(boolean status) {
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }
}
