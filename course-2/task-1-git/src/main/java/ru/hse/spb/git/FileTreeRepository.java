package ru.hse.spb.git;

import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
class FileTreeRepository {
    private static final String MARKER = "tree\0";
    private static final int MARKER_LENGTH = MARKER.getBytes().length;
    private static final Charset ENCODING = StandardCharsets.UTF_8;
    private final Path root;

    @NotNull
    public FileTree createTree(List<FileRef> refs) throws IOException {
        String hash = hashTree(refs);
        if (exists(hash)) {
            throw new IllegalArgumentException("File tree with " + hash + " already exists!");
        }

        Path blobFile = Files.createFile(root.resolve(hash));

        try (InputStream stream = withMarker(serializeReferences(refs))) {
            Files.copy(stream, blobFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return new FileTree(hash, refs);
    }

    public boolean exists(String hash) {
        return Files.exists(root.resolve(hash));
    }

    public Optional<FileTree> getTree(String hash) throws IOException {
        if (!exists(hash)) {
            return Optional.empty();
        }

        try (InputStream inputStream = Files.newInputStream(root.resolve(hash))) {
            assert MARKER_LENGTH == inputStream.skip(MARKER_LENGTH) : "No " + MARKER + " present in file!";

            List<FileRef> collect = deserializeReferences(inputStream);

            return Optional.of(new FileTree(hash, collect));
        }
    }

    @NotNull
    public String hashTree(List<FileRef> refs) throws IOException {
        if (refs.isEmpty()) {
            throw new IllegalStateException("Cannot create empty file tree!");
        }

        try (InputStream stream = withMarker(serializeReferences(refs))) {
            return DigestUtils.sha1Hex(stream);
        }
    }

    @NotNull
    private InputStream withMarker(InputStream data) throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(MARKER, ENCODING),
            data
        );
    }

    private InputStream serializeReferences(List<FileRef> refs) throws IOException {
        String serializedText = refs.stream()
            .map(this::serializeReference)
            .collect(Collectors.joining(System.getProperty("line.separator")));

        return IOUtils.toInputStream(serializedText, "UTF-8");
    }

    private List<FileRef> deserializeReferences(InputStream inputStream) throws IOException {
        return IOUtils.readLines(inputStream, ENCODING).stream()
            .map(this::deserializeRef)
            .collect(Collectors.toList());
    }

    private String serializeReference(FileRef r) {
        return String.format("%s %s %s", r.getHash(), r.getType().name(), r.getName());
    }

    private FileRef deserializeRef(String line) {
        String[] args = line.split(" ", 3);
        return new FileRef(args[0], FileRef.Type.valueOf(args[1]), args[2]);
    }

}
