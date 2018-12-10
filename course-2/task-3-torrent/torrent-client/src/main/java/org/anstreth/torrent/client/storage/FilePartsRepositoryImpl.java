package org.anstreth.torrent.client.storage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

public class FilePartsRepositoryImpl implements FilePartsRepository {
    private Path folder;

    public FilePartsRepositoryImpl(Path rootFolder) {
        folder = rootFolder;
    }

    @Override
    public List<FilePartsDetails> listFiles() throws IOException {
        List<Path> partsFile = Files.list(folder)
            .filter(f -> Files.isRegularFile(f))
            .filter(f -> f.getFileName().toString().endsWith(".parts"))
            .collect(Collectors.toList());

        List<FilePartsDetails> results = new ArrayList<>();
        for (Path path : partsFile) {
            int fileId = Integer.parseInt(path.getFileName().toString().split("\\.")[0]);
            results.add(getFile(fileId));
        }

        results.sort(Comparator.comparing(FilePartsDetails::getFileId));
        return results;
    }

    @Override
    public void addFileWithAllParts(int fileId, int numberOfParts) throws IOException {
        assertPartsAreCorrect(numberOfParts);

        Set<Integer> parts = new HashSet<>();
        for (int i = 0; i < numberOfParts; i++) {
            parts.add(i);
        }

        storeParts(new FilePartsDetails(fileId, numberOfParts, parts));
    }

    @Override
    public void addFileWithoutParts(int fileId, int numberOfParts) throws IOException {
        assertPartsAreCorrect(numberOfParts);

        storeParts(new FilePartsDetails(fileId, numberOfParts, emptySet()));
    }

    @Override
    public FilePartsDetails getFile(int fileId) throws IOException {
        return readParts(fileId);
    }

    @Override
    public void savePart(int fileId, int partNumber) throws IOException {
        FilePartsDetails parts = readParts(fileId);

        if (partNumber < 0 || partNumber >= parts.getNumberOfParts()) {
            throw new IllegalArgumentException(String.format(
                "File %d contains parts from 0 to %d; cannot add part %d",
                fileId,
                parts.getNumberOfParts(),
                partNumber
            ));
        }

        Set<Integer> updatedParts = new HashSet<>(parts.getReadyParts());
        if (!updatedParts.add(partNumber)) {
            throw new IllegalArgumentException(String.format("File %d already contains part %d", fileId, partNumber));
        }

        FilePartsDetails updated = new FilePartsDetails(
            parts.getFileId(),
            parts.getNumberOfParts(),
            updatedParts
        );
        storeParts(updated);
    }

    private void assertPartsAreCorrect(int numberOfParts) {
        if (numberOfParts <= 0) {
            throw new IllegalArgumentException(String.format("Cannot create file with %d parts", numberOfParts));
        }
    }

    private void storeParts(FilePartsDetails parts) throws IOException {
        Path file = getPartsFile(parts.getFileId());
        try (DataOutputStream dataOutputStream = new DataOutputStream(Files.newOutputStream(file))) {
            dataOutputStream.writeInt(parts.getNumberOfParts());
            writePartsInfo(dataOutputStream, parts.getReadyParts());
        }
    }

    private FilePartsDetails readParts(int fileId) throws IOException {
        Path file = getPartsFile(fileId);
        try (DataInputStream inputStream = new DataInputStream(Files.newInputStream(file))) {
            int numberOfParts = inputStream.readInt();
            Set<Integer> availableParts = readPartsInfo(inputStream);

            return new FilePartsDetails(fileId, numberOfParts, availableParts);
        }
    }

    private Set<Integer> readPartsInfo(DataInputStream inputStream) throws IOException {
        Set<Integer> result = new HashSet<>();
        int count = inputStream.readInt();
        for (int i = 0; i < count; i++) {
            int part = inputStream.readInt();
            result.add(part);
        }

        return result;
    }

    private void writePartsInfo(DataOutputStream outputStream, Set<Integer> availableParts) throws IOException {
        outputStream.writeInt(availableParts.size());
        for (int integer : availableParts) {
            outputStream.writeInt(integer);
        }
    }

    private Path getPartsFile(int fileId) {
        return folder.resolve(String.format("%d.parts", fileId));
    }
}
