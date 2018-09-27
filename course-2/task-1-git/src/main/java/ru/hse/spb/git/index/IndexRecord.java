package ru.hse.spb.git.index;

import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static ru.hse.spb.git.CollectionUtils.toStream;

@Data
public class IndexRecord {
    private final String hash;
    private final List<String> pathParts;

    public Path getPath() {
        return Paths.get("", pathParts.toArray(new String[0]));
    }

    static public IndexRecord fromPath(String hash, Path path) {
        return new IndexRecord(
            hash,
            toStream(path.iterator()).map(Path::toString).collect(Collectors.toList())
        );
    }
}
