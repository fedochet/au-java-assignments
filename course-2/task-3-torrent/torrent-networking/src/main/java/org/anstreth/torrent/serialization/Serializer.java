package org.anstreth.torrent.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface Serializer<T> {
    default byte[] serialize(T value) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        serialize(value, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    void serialize(T value, OutputStream stream) throws IOException;
}
