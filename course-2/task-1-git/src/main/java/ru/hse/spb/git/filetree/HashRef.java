package ru.hse.spb.git.filetree;

import lombok.Data;

@Data
public final class HashRef {
    public enum Type {
        REGULAR_FILE, DIRECTORY
    }

    private final String hash;
    private final Type type;
    private final String name;
}
