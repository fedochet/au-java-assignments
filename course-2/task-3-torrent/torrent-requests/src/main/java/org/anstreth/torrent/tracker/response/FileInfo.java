package org.anstreth.torrent.tracker.response;

import java.util.Objects;

public class FileInfo {
    private final int id;
    private final String name;
    private final long size;

    public FileInfo(int id, String name, long size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInfo fileInfo = (FileInfo) o;
        return id == fileInfo.id &&
            size == fileInfo.size &&
            Objects.equals(name, fileInfo.name);
    }
}
