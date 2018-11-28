package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.tracker.response.FileInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileInfoDeserializer implements Deserializer<FileInfo> {
    @Override
    public FileInfo deserialize(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        String name = dataInputStream.readUTF();
        long size = dataInputStream.readLong();
        return new FileInfo(name, size);
    }
}
