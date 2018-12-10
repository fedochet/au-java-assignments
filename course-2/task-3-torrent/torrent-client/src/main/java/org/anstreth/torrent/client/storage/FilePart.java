package org.anstreth.torrent.client.storage;

import java.util.Objects;

public class FilePart {
    private final int fileId;
    private final int number;

    FilePart(int fileId, int number) {
        this.fileId = fileId;
        this.number = number;
    }

    public int getFileId() {
        return fileId;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilePart filePart = (FilePart) o;
        return fileId == filePart.fileId &&
            number == filePart.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, number);
    }
}
