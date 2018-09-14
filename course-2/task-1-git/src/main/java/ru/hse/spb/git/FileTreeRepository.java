package ru.hse.spb.git;

import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
class FileTreeRepository {
    private static final String MARKER = "tree\0";
    private static final int MARKER_LENGTH = MARKER.getBytes().length;
    private static final String ENCODING = "UTF-8";
    private final Path root;

    FileTree createTree(List<FileRef> refs) throws IOException {
        String hash = hashTree(refs);
        if (exists(hash)) {
            throw new IllegalArgumentException("File tree with " + hash + " already exists!");
        }

        Path blobFile = Files.createFile(root.resolve(hash));

        String serializedRefs = refs.stream()
            .map(r -> String.format("%s %s %s", r.getHash(), r.getType().name(), r.getName()))
            .collect(Collectors.joining(System.getProperty("line.separator")));

        try (InputStream stream = withMarker(serializedRefs)) {
            Files.copy(stream, blobFile);
        }

        return new FileTree(hash, refs);
    }


    public boolean exists(String hash) {
        return Files.exists(root.resolve(hash));
    }

    Optional<FileTree> getTree(String hash) throws IOException {
        if (!exists(hash)) {
            return Optional.empty();
        }

        try (InputStream inputStream = Files.newInputStream(root.resolve(hash))) {
            assert MARKER_LENGTH == inputStream.skip(MARKER_LENGTH) : "No " + MARKER + " present in file!";

            List<FileRef> collect =
                IOUtils.readLines(inputStream, ENCODING).stream()
                .map(line -> line.split(" ", 3))
                .map(args -> new FileRef(args[0], FileRef.Type.valueOf(args[1]), args[2]))
                .collect(Collectors.toList());

            return Optional.of(new FileTree(hash, collect));
        }
    }

    String hashTree(List<FileRef> refs) throws IOException {
        if (refs.isEmpty()) {
            throw new IllegalStateException("Cannot create empty file tree!");
        }

        String serializedRefs = refs.stream()
            .map(r -> String.format("%s %s %s", r.getHash(), r.getType().name(), r.getName()))
            .collect(Collectors.joining(System.getProperty("line.separator")));

        return DigestUtils.sha1Hex(withMarker(serializedRefs));
    }

    private SequenceInputStream withMarker(String serializedRefs) throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(MARKER, ENCODING),
            IOUtils.toInputStream(serializedRefs, "UTF-8")
        );
    }

}
