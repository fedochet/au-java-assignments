package ru.hse.spb.git.filetree;

import lombok.Data;

@Data
public final class HashRef {
    public enum Type {
        FILE, DIRECTORY
    }

    private final String hash;
    private final Type type;
    private final String name;

    public static HashRef file(String hash, String name) {
        return new HashRef(hash, Type.FILE, name);
    }

    public static HashRef directory(String hash, String name) {
        return new HashRef(hash, Type.DIRECTORY, name);
    }

}
