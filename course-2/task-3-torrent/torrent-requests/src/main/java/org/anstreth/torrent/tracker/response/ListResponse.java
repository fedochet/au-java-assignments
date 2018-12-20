package org.anstreth.torrent.tracker.response;

import java.util.ArrayList;
import java.util.List;

public class ListResponse {
    private final List<FileInfo> files;

    public ListResponse(List<FileInfo> files) {
        this.files = new ArrayList<>(files);
    }

    public List<FileInfo> getFiles() {
        return files;
    }
}
