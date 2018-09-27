package ru.hse.spb.git.index;

import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Data
public class FileReference {
    private final String hash;
    private final List<String> pathParts;

    public Path getPath() {
        return Paths.get("", pathParts.toArray(new String[0]));
    }

    static public FileReference fromPath(String hash, Path path) {
        List<String> pathParts = new ArrayList<>();
        for (Path part : path) {
            pathParts.add(part.toString());
        }

        return new FileReference(hash, pathParts);
    }
}
