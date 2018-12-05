package org.anstreth.torrent.tracker.response;

public class UpdateResponse {
    private final boolean successful;

    public UpdateResponse(boolean successful) {
        this.successful = successful;
    }

    public boolean isSuccessful() {
        return successful;
    }
}
