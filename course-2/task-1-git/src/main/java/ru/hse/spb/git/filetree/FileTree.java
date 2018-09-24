package ru.hse.spb.git.filetree;

import lombok.Data;

import java.util.List;

@Data
public final class FileTree {
    private final String hash;
    private final List<FileRef> children;
}
