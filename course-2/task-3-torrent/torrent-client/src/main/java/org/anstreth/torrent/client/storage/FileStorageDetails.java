package org.anstreth.torrent.client.storage;

import java.nio.file.Path;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FileStorageDetails {
    private final int fileId;
    private final Path file;
    private final int partsNumber;
    private final BitSet partsStatuses;

    public FileStorageDetails(int fileId, Path file, int partsNumber, BitSet partsStatuses) {
        this.fileId = fileId;
        this.file = file;
        this.partsNumber = partsNumber;
        this.partsStatuses = partsStatuses;
    }

    public Path getFile() {
        return file;
    }

    public List<FilePart> availableParts() {
        return IntStream.range(0, partsNumber)
            .filter(partsStatuses::get)
            .mapToObj(i -> new FilePart(fileId, i))
            .collect(Collectors.toList());
    }

    public List<FilePart> missingParts() {
        return IntStream.range(0, partsNumber)
            .filter(i -> !partsStatuses.get(i))
            .mapToObj(i -> new FilePart(fileId, i))
            .collect(Collectors.toList());
    }

    void finishPart(int number) {
        partsStatuses.set(number);
    }

    int getFileId() {
        return fileId;
    }
}
