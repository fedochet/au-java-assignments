package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.tracker.response.FileInfo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileInfoSerializer implements Serializer<FileInfo> {
    @Override
    public void serialize(FileInfo value, OutputStream stream) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(stream);
        dataOutputStream.writeUTF(value.getName());
        dataOutputStream.writeLong(value.getSize());
    }
}
