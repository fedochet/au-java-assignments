package ru.hse.spb.git;

import lombok.Data;

import java.util.List;

@Data
final class FileTree {
    private final String hash;
    private final List<FileRef> children;
}
