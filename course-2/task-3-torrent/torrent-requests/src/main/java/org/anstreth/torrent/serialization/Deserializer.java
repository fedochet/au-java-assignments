package org.anstreth.torrent.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface Deserializer<T> {
    default T deserialize(byte[] inputStream) throws IOException {
        return deserialize(new ByteArrayInputStream(inputStream));
    }

    T deserialize(InputStream inputStream) throws IOException;
}
