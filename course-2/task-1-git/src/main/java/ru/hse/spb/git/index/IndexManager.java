package ru.hse.spb.git.index;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

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

    public List<FileReference> getAllRecords() throws IOException {
        try (Stream<FileReference> indexEntries = getIndexEntries()) {
            return indexEntries.sorted(Comparator.comparing(FileReference::getPath)).collect(Collectors.toList());
        }
    }

    public Optional<FileReference> get(Path path) throws IOException {
        try (Stream<FileReference> indexEntries = getIndexEntries()) {
            return indexEntries
                .filter(ioPredicate(record -> repositoryRoot.resolve(record.getPath()).equals(path)))
                .findFirst();
        }
    }

    public void set(Path path, String hash) throws IOException {
        List<FileReference> fileReferences = new ArrayList<>(getAllRecords());
        fileReferences.removeIf(record -> repositoryRoot.resolve(record.getPath()).equals(path));
        fileReferences.add(FileReference.fromPath(hash, repositoryRoot.relativize(path)));
        updateIndex(fileReferences);
    }

    public void delete(Path path) throws IOException {
        List<FileReference> fileReferences = new ArrayList<>(getAllRecords());
        fileReferences.removeIf(record -> repositoryRoot.resolve(record.getPath()).equals(path));
        updateIndex(fileReferences);
    }

    private void updateIndex(List<FileReference> records) throws IOException {
        List<String> serialized = records.stream()
            .map(this::serializeReference)
            .collect(Collectors.toList());

        try (OutputStream fileOutputStream = Files.newOutputStream(indexFile)) {
            IOUtils.writeLines(serialized, "\n", fileOutputStream, ENCODING);
        }
    }

    private Stream<FileReference> getIndexEntries() throws IOException {
        return Files.lines(indexFile, ENCODING).map(this::deserializeReference);
    }

    @NotNull
    private String serializeReference(FileReference reference) {
        return String.format(
            "%s %s",
            reference.getHash(),
            String.join("/", reference.getPathParts())
        );
    }

    @NotNull
    private FileReference deserializeReference(String line) {
        String[] splitted = line.split(" ", 2);
        List<String> pathParts = Arrays.asList(splitted[1].split("/"));
        return new FileReference(splitted[0], pathParts);
    }
}
