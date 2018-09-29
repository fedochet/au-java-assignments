package ru.hse.spb.git.index;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import ru.hse.spb.git.filetree.FileTreeRepository;
import ru.hse.spb.git.filetree.HashRef;

import java.io.IOException;
import java.util.*;

enum Type {
    FILE, DIRECTORY
}

interface VirtualEntry {
    @NotNull
    Type getType();

    @NotNull
    String getName();
}

@Data
final class VirtualFile implements VirtualEntry {
    private final String hash;
    private final String name;

    @NotNull
    @Override
    public Type getType() {
        return Type.FILE;
    }
}

@Data
final class VirtualDirectory implements VirtualEntry {
    private final String name;
    private final VirtualFileTree fileTree;

    @NotNull
    @Override
    public Type getType() {
        return Type.DIRECTORY;
    }
}

public final class VirtualFileTree {
    private final Map<String, VirtualEntry> children = new HashMap<>();

    public void addFile(FileReference reference) {
        addFile(reference, reference.getPathParts().iterator());
    }

    /**
     * Creates file tree using passed repository. If does not contain any file, returns empty Optional.
     */
    public Optional<String> buildFileTree(FileTreeRepository repository) throws IOException {
        List<HashRef> refs = new ArrayList<>();

        for (VirtualEntry entry : children.values()) {
            if (entry.getType().equals(Type.DIRECTORY)) {
                VirtualDirectory directory = (VirtualDirectory) entry;
                directory.getFileTree().buildFileTree(repository)
                    .map(treeHash -> HashRef.directory(treeHash, directory.getName()))
                    .ifPresent(refs::add);
            } else {
                VirtualFile file = ((VirtualFile) entry);
                refs.add(HashRef.file(file.getHash(), file.getName()));
            }
        }

        if (refs.isEmpty()) {
            return Optional.empty();
        }

        String treeHash = repository.hashTree(refs);
        if (repository.exists(treeHash)) {
            return Optional.of(treeHash);
        }

        return Optional.of(repository.createTree(refs).getHash());
    }

    private void addFile(FileReference reference, Iterator<String> pathParts) {
        if (!pathParts.hasNext()) {
            throw new IllegalArgumentException(String.format("File reference %s with empty path!", reference));
        }

        String current = pathParts.next();
        if (pathParts.hasNext()) {
            addFileToDirectory(reference, current, pathParts);
        } else {
            saveFile(reference, current);
        }
    }

    private void saveFile(FileReference reference, String fileName) {
        VirtualFile newFile = new VirtualFile(reference.getHash(), fileName);
        VirtualEntry prevFile = children.put(fileName, newFile);
        if (prevFile != null) {
            throw new IllegalArgumentException(String.format(
                "Cannot add %s to virtual file tree because it already existed: %s",
                newFile,
                prevFile
            ));
        }
    }

    private void addFileToDirectory(FileReference reference, String directory, Iterator<String> pathParts) {
        VirtualEntry nextFile = children.computeIfAbsent(directory, name ->
            new VirtualDirectory(name, new VirtualFileTree())
        );

        if (nextFile.getType().equals(Type.DIRECTORY)) {
            ((VirtualDirectory) nextFile).getFileTree().addFile(reference, pathParts);
        } else {
            throw new IllegalArgumentException(String.format(
                "Cannot add %s to virtual file tree because cannot create folder with name %s!",
                reference,
                directory
            ));
        }
    }
}
