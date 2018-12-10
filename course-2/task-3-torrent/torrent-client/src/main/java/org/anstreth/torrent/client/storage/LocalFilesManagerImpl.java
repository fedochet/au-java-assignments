package org.anstreth.torrent.client.storage;

import org.apache.commons.io.input.BoundedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// FIXME: 09.12.18 persist info between launches
public class LocalFilesManagerImpl implements LocalFilesManager {
    private final long partSize;
    private final Path rootDir;

    private final FileStorageDetailsFabric fabric;

    private final Map<Integer, FileStorageDetails> files = new HashMap<>();

    public LocalFilesManagerImpl(long partSize, Path rootDir) {
        this.partSize = partSize;
        // FIXME: 08.12.18 check if this is directory
        this.rootDir = rootDir;
        fabric = new FileStorageDetailsFabric(this.partSize);
    }

    @Override
    public void registerFile(int fileId, Path path) throws IOException {
        files.put(fileId, fabric.completedFileDetails(fileId, path));
    }

    @Override
    public void allocateFile(int fileId, String name, long size) throws IOException {
        Path file = rootDir.resolve(name);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.toFile(), "rw")) {
            randomAccessFile.setLength(size);
        }

        files.put(fileId, fabric.newFileDetails(fileId, file));
    }

    @Override
    public List<FileStorageDetails> listFiles() throws IOException {
        return files.values().stream()
            .sorted(Comparator.comparing(FileStorageDetails::getFileId))
            .collect(Collectors.toList());
    }

    @Override
    public List<FilePart> getAvailableParts(int fileId) throws IOException {
        return files.get(fileId).availableParts();
    }

    @Override
    public InputStream openForReading(FilePart part) throws IOException {
        FileStorageDetails fileStorageDetails = files.get(part.getFileId());

        FileChannel channel = FileChannel
            .open(fileStorageDetails.getFile(), StandardOpenOption.READ)
            .position(partSize * part.getNumber());

        return new BoundedInputStream(Channels.newInputStream(channel), partSize);
    }

    @Override
    public OutputStream openForWriting(FilePart part) throws IOException {
        Path path = files.get(part.getFileId()).getFile();
        FileChannel channel = FileChannel
            .open(path, StandardOpenOption.WRITE)
            .position(partSize * part.getNumber());

        // FIXME check that no more than partSize is written
        return Channels.newOutputStream(channel);
    }

    @Override
    public void finishFilePart(FilePart part) throws IOException {
        files.get(part.getFileId()).finishPart(part.getNumber());
    }
}
