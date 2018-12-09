package org.anstreth.torrent.client.storage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class FileStorageDetails {
    private final int fileId;
    private final Path file;
    private final BitSet partsStatuses;

    public FileStorageDetails(int fileId, Path file, BitSet partsStatuses) {
        this.fileId = fileId;
        this.file = file;
        this.partsStatuses = partsStatuses;
    }

    public Path getFile() {
        return file;
    }

    public List<FilePart> availableParts() {
        List<FilePart> objects = new ArrayList<>(partsStatuses.size());
        for (int i = 0; i < partsStatuses.size(); i++) {
            if (partsStatuses.get(i)) {
                objects.add(new FilePart(fileId, i));
            }
        }

        return objects;
    }

    public void finishPart(int number) {
        partsStatuses.set(number);
    }

    int getFileId() {
        return fileId;
    }
}
