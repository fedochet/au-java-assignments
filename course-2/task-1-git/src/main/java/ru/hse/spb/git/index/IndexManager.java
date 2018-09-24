package ru.hse.spb.git.index;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.hse.spb.git.CollectionUtils.ioPredicate;

public class IndexManager {
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    private final Path repositoryRoot;
    private final Path indexFile;

    public IndexManager(Path repositoryRoot, Path indexFile) {
        this.repositoryRoot = repositoryRoot;
        this.indexFile = indexFile;
    }

    public void updateIndex(List<IndexRecord> records) throws IOException {
        List<String> serialized = records.stream()
            .map(record -> String.format(
                "%s %s",
                record.getHash(),
                String.join("/", record.getPathParts())
            ))
            .collect(Collectors.toList());

        try (OutputStream fileOutputStream = Files.newOutputStream(indexFile)) {
            IOUtils.writeLines(serialized, "\n", fileOutputStream, ENCODING);
        }
    }

    public List<IndexRecord> getAllRecords() throws IOException {
        try (Stream<IndexRecord> indexEntries = getIndexEntries()) {
            return indexEntries.sorted(Comparator.comparing(IndexRecord::getPath)).collect(Collectors.toList());
        }
    }

    public Optional<IndexRecord> get(Path path) throws IOException {
        try (Stream<IndexRecord> indexEntries = getIndexEntries()) {
            return indexEntries
                .filter(ioPredicate(record -> repositoryRoot.resolve(record.getPath()).equals(path)))
                .findFirst();
        }
    }

    public void set(Path path, String hash) throws IOException {
        List<IndexRecord> indexRecords = new ArrayList<>(getAllRecords());
        indexRecords.removeIf(record -> repositoryRoot.resolve(record.getPath()).equals(path));
        indexRecords.add(IndexRecord.fromPath(hash, repositoryRoot.relativize(path)));
        updateIndex(indexRecords);
    }

    public void delete(Path path) throws IOException {
        List<IndexRecord> indexRecords = new ArrayList<>(getAllRecords());
        indexRecords.removeIf(record -> repositoryRoot.resolve(record.getPath()).equals(path));
        updateIndex(indexRecords);
    }

    private Stream<IndexRecord> getIndexEntries() throws IOException {
        return Files.lines(indexFile, ENCODING).map(line -> {
            String[] splitted = line.split(" ", 2);
            List<String> pathParts = Arrays.asList(splitted[1].split("/"));
            return new IndexRecord(splitted[0], pathParts);
        });
    }
}
