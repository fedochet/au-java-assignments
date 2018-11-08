package ru.hse.spb.git;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class TempDirectoryTestBase {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    Path createFile(Path file, String content) throws IOException {
        Path newFile = tempFolder.getRoot().toPath().resolve(file);
        Files.createDirectories(newFile.getParent());
        Files.write(newFile, content.getBytes(UTF_8));

        return newFile;
    }

    Path createFile(String name, String content) throws IOException {
        return createFile(Paths.get(name), content);
    }
}
