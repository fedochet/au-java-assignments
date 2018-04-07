package ru.spbau.task3;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StreamSerializable {

    void serialize(@NotNull OutputStream out) throws IOException;

    /**
     * Replace current state with data from input stream
     */
    void deserialize(@NotNull InputStream in) throws IOException;
}
