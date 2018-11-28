package org.anstreth.torrent.tracker.response;

public class FileInfo {
    private final String name;
    private final long size;

    public FileInfo(String name, long size) {
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
