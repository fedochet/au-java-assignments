package org.anstreth.torrent.serialization;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SerializationUtils {
    private SerializationUtils() {
    }

    public static DataInputStream getDataInputStream(InputStream in) {
        if (in instanceof DataInputStream) {
            return (DataInputStream) in;
        }

        return new DataInputStream(in);
    }

    public static DataOutputStream getDataOutputStream(OutputStream out) {
        if (out instanceof DataOutputStream) {
            return (DataOutputStream) out;
        }

        return new DataOutputStream(out);
    }

    static <T> void serializeList(List<T> list,
                                         Serializer<T> serializer,
                                         OutputStream stream) throws IOException {
        DataOutputStream dataOutputStream = getDataOutputStream(stream);
        dataOutputStream.writeInt(list.size());
        for (T file : list) {
            serializer.serialize(file, dataOutputStream);
        }
    }

    static <T> List<T> deserializeList(Deserializer<T> deserializer,
                                              InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = getDataInputStream(inputStream);
        int size = dataInputStream.readInt();
        List<T> objects = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            objects.add(deserializer.deserialize(inputStream));
        }

        return objects;
    }
}
