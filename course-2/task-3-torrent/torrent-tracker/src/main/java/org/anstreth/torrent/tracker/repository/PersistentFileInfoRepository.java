package org.anstreth.torrent.tracker.repository;

import org.anstreth.torrent.tracker.response.FileInfo;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

// TODO: 04.12.18 add some kind of synchronisation on file
public class PersistentFileInfoRepository implements FileInfoRepository {
    private final Path persistenceFile;
    private final AtomicInteger counter;

    public PersistentFileInfoRepository(Path persistenceFile) {
        this.persistenceFile = persistenceFile;
        OptionalInt maxPreviousId = getAllFiles().stream().mapToInt(FileInfo::getId).max();
        counter = new AtomicInteger(maxPreviousId.orElse(0) + 1);
    }

    @Override
    public int addFile(@NotNull String fileName, long fileSize) {
        int fileId = generateId();

        try {
            appendFileInfo(String.format("%d:%d:%s", fileId, fileSize, fileName));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot add file!", e);
        }

        return fileId;
    }

    @Override
    public @NotNull List<FileInfo> getAllFiles() {
        List<String> lines;

        try {
            lines = Files.readAllLines(persistenceFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read files!", e);
        }

        return lines.stream()
            .map(this::parseFileInfo)
            .sorted(Comparator.comparing(FileInfo::getId))
            .collect(Collectors.toList());
    }

    private FileInfo parseFileInfo(String line) {
        String[] data = line.split(":", 3);
        int id = Integer.parseInt(data[0]);
        int size1 = Integer.parseInt(data[1]);
        String name = data[2];

        return new FileInfo(id, name, size1);
    }

    private void appendFileInfo(String line) throws IOException {
        try (BufferedWriter writer =
                 Files.newBufferedWriter(persistenceFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            writer.write(line);
            writer.newLine();
        }
    }

    private int generateId() {
        return counter.getAndIncrement();
    }
}
