package org.anstreth.torrent.client.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class FileStorageDetailsFabric {
    private final long partSize;

    public FileStorageDetailsFabric(long partSize) {
        this.partSize = partSize;
    }

    public FileStorageDetails completedFileDetails(int fileId, Path path) throws IOException {
        BitSet bitSet = new BitSet(numberOfParts(path));
        bitSet.flip(0, bitSet.size());
        return new FileStorageDetails(fileId, path, bitSet);
    }

    public FileStorageDetails newFileDetails(int fileId, Path path) throws IOException {
        return partialFileDetails(fileId, path, Collections.emptyList());
    }

    public FileStorageDetails partialFileDetails(int fileId, Path path, List<Integer> readyParts) throws IOException {
        int numberOfParts = numberOfParts(path);
        BitSet bitSet = new BitSet(numberOfParts);
        readyParts.forEach(bitSet::set);
        return new FileStorageDetails(fileId, path, bitSet);
    }

    private int numberOfParts(Path path) throws IOException {
        return (int) Math.ceil(Files.size(path) / (float) partSize);
    }
}
