package org.anstreth.torrent.tracker.request;

import java.util.List;

public class UpdateRequest {
    private final short port;
    private final List<Integer> fileIds;

    public UpdateRequest(short port, List<Integer> fileIds) {
        this.port = port;
        this.fileIds = fileIds;
    }

    public short getPort() {
        return port;
    }

    public List<Integer> getFileIds() {
        return fileIds;
    }
}
