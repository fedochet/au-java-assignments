package ru.hse.spb.git;

import lombok.Data;

@Data
final class FileRef {
    public enum Type {
        REGULAR_FILE, DIRECTORY
    }

    private final String hash;
    private final Type type;
    private final String name;
}
