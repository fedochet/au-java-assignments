package org.anstreth.torrent.client.storage;

import org.apache.commons.io.input.BoundedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class LocalFilesManagerImpl implements LocalFilesManager {
    private final long partSize;
    private final Path rootDir;
    private final FilePartsRepository partsRepository;

    public LocalFilesManagerImpl(long partSize, Path rootDir) throws IOException {
        this.partSize = partSize;
        this.rootDir = rootDir;
        Path partsDir = Files.createDirectories(rootDir.resolve(".parts"));
        partsRepository = new FilePartsRepositoryImpl(partsDir);
    }

    @Override
    public void registerFile(int fileId, Path path) throws IOException {
        partsRepository.addFileWithAllParts(fileId, path, numberOfParts(path));
    }

    @Override
    public void allocateFile(int fileId, String name, long size) throws IOException {
        Path file = rootDir.resolve(name);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.toFile(), "rw")) {
            randomAccessFile.setLength(size);
        }

        partsRepository.addFileWithoutParts(fileId, file, numberOfParts(file));
    }

    @Override
    public List<FilePartsDetails> listFiles() throws IOException {
        return partsRepository.listFiles();
    }

    @Override
    public Set<FilePart> getAvailableParts(int fileId) throws IOException {
        return partsRepository.getFile(fileId).getReadyParts();
    }

    @Override
    public InputStream openForReading(FilePart part) throws IOException {
        FilePartsDetails fileStorageDetails = partsRepository.getFile(part.getFileId());

        FileChannel channel = FileChannel
            .open(fileStorageDetails.getFile(), StandardOpenOption.READ)
            .position(partSize * part.getNumber());

        return new BoundedInputStream(Channels.newInputStream(channel), partSize);
    }

    @Override
    public OutputStream openForWriting(FilePart part) throws IOException {
        Path path = partsRepository.getFile(part.getFileId()).getFile();
        FileChannel channel = FileChannel
            .open(path, StandardOpenOption.WRITE)
            .position(partSize * part.getNumber());

        // FIXME check that no more than partSize is written
        return Channels.newOutputStream(channel);
    }

    @Override
    public void finishFilePart(FilePart part) throws IOException {
        partsRepository.savePart(part.getFileId(), part.getNumber());
    }

    private int numberOfParts(Path path) throws IOException {
        return (int) Math.ceil(Files.size(path) / (float) partSize);
    }
}
