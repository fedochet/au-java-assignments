package org.anstreth.torrent.client.storage;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FilePartsDetails {
    private final int fileId;
    private final Path file;
    private final int numberOfParts;
    private final Set<Integer> readyParts;

    FilePartsDetails(int fileId, Path file, int numberOfParts, Set<Integer> readyParts) {
        this.fileId = fileId;
        this.file = file;
        this.numberOfParts = numberOfParts;
        this.readyParts = readyParts;
    }

    public int getFileId() {
        return fileId;
    }

    public int getNumberOfParts() {
        return numberOfParts;
    }

    public Set<Integer> getReadyPartsIndexes() {
        return readyParts;
    }

    public Set<FilePart> getReadyParts() {
        return readyParts.stream()
            .map(this::partOfThisFile)
            .collect(Collectors.toSet());
    }

    public Set<FilePart> getMissingParts() {
        return IntStream.range(0, numberOfParts).boxed()
            .filter(i -> !readyParts.contains(i))
            .map(this::partOfThisFile)
            .collect(Collectors.toSet());
    }

    private FilePart partOfThisFile(Integer i) {
        return new FilePart(fileId, i);
    }

    public Path getFile() {
        return file;
    }
}
